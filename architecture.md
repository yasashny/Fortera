# Архитектура Fortera


## Слои

```
app                  точка сборки графа: Koin + ForteraNavHost
 ↓
feature/*            экраны, ViewModel, локальный домен фичи
 ↓ (зависит от)
core/wallet-balances бридж: wallet + crypto, сид-фраза наружу не течёт
 ↓
core/domain-*        контракты, чистый Kotlin   ←реализуют→   core/data-*
 ↓
core/common · network · database · mvi · navigation · designsystem · ui

feature/*-api        листья: только @Serializable ключи навигации
```

feature зависит от `*-api` других фич, от бриджа, от domain-*, от core/ui+mvi+navigation.
На data-* и на чужие impl — никогда.

## Фича = два Gradle-модуля

- `feature:foo-api` — только `@Serializable` nav-ключи и публичные enum. Ни логики, ни
  Koin. Зависеть может кто угодно.
- `feature:foo` — реализация: ViewModel, Composable, Koin-модуль, регистрация
  `FeatureNavProvider`.

Навигация между фичами всегда через `-api`: `feature:foo` тянет `feature:bar-api`, но не
`feature:bar`. Так граф фич остаётся ацикличным.

## Бридж: core/wallet-balances

Сервисы поверх доменов wallet и crypto. Сид-фраза и низкоуровневые примитивы наружу не
идут.

- `WalletAddressesService` — адреса ETH/BTC по id кошелька.
- `WalletBalances` — наблюдение и обновление балансов.
- `WalletTransactionSender` — подпись и отправка.

Фичи ходят сюда, а не в репозитории напрямую. Сид-фраза живёт только в бридже и в двух
фичах, которые физически создают кошелёк (`create-wallet`, `import-wallet`). Всё остальное
оперирует walletId и публичными адресами.

## domain-* и data-*

`core/domain-*` — чистый Kotlin: data-классы, интерфейсы, enum. Никакого Android (кроме
`core/common`), никаких web3j/bitcoinj/ktor/room.

- `domain-wallet`: `Wallet`, `SeedPhrase`, `WalletRepository`, `WalletInteractor`.
- `domain-crypto`: `TokenDefinition`, `TokenBalance`, `BlockchainNetwork`, `AddressResolver`,
  репозитории балансов/цен/транзакций (включая отправку), `FetchPolicy`, `TokenCatalog`.

`core/data-*` — реализации: Room DAO, Ktor-источники, HD-деривация.

- `data-crypto`: `HdWallet`, `AddressResolverImpl`, Room, источники (Infura, Blockstream,
  CoinStats), все `*RepositoryImpl`, `EthereumSender`, `BitcoinSender`, `dataCryptoModule`.
- Дата-слой кошелька пока лежит в `domain-wallet` (Room) — он мал, отдельный `data-wallet`
  не нужен. Вынесем, если домен разрастётся.

Фичи знают только контракты. Impl подключает `app` на этапе сборки.

## Инфраструктура (core/*)

- `common` — база `Interactor`, `ForteraDispatchers`, `Haptics`.
- `network` — фабрика `HttpClient`, `EnvironmentRepository` (mainnet/testnet), `InMemoryCache`.
- `database` — `SecureStorage` (EncryptedSharedPreferences), DataStore.
- `mvi` — `MviViewModel`, `IntentScope`, `MviContainer`. Единственный MVI-фреймворк в проекте.
- `navigation` — `AppNavigator`, `NavigationRegistry`, `FeatureNavProvider`, `ForteraNavHost`.
- `designsystem` — тема, цвета, типографика.
- `ui` — общие Composable (`GroupCard`, `ShimmerBox`, `ForteraCollapsingScaffold`,
  `InputCard`), хелперы иконок токенов и форматирования денег.

## Паттерны

### MVI — один поток состояния на экран

`MviViewModel<S, I, E>`: наружу `state: StateFlow<S>`, `effect: Flow<E>`, `currentState` и
`onIntent`. Внутри `handleIntent`, `updateState`, `reduce`, `sendEffect`, `handleError`.

Контракты — три sealed-типа, каждый в своём файле: `FooState` (UiState), `FooIntent`
(UiIntent), `FooEffect` (UiEffect). При сложной логике рядом кладётся `FooReducer` (чистые
функции), `FooViewModel` остаётся тонким.

Всё, что должно пережить пересоздание, — в `FooState`: баннеры, диалоги, флаги загрузки,
видимость шторок. `FooEffect` — только одноразовое: навигация, snackbar, буфер, хаптика.

### Screen = контейнер, Layout = чистый рендер

```kotlin
@Composable
fun FooScreen(viewModel: FooViewModel = koinViewModel()) {
    MviContainer(viewModel, onEffect = { ... }) { state, onIntent ->
        FooLayout(state, onIntent)
    }
}

@Composable
internal fun FooLayout(state: FooState, onIntent: (FooIntent) -> Unit) { ... }
```

Диалоги, шторки и snackbar по условию из state рисует Layout — он единственный источник
рендера и превьюится без ViewModel. В `onEffect` Screen-а только то, чему нужен navigator
или системное API.

### Навигация — фичи регистрируются сами

```kotlin
interface FeatureNavProvider {
    fun entryFor(key: Any): NavEntry<*>?   // null, если ключ чужой
}
class NavigationRegistry(providers: List<FeatureNavProvider>)
```

Каждая фича регистрирует свой `FeatureNavProvider` в Koin-модуле, `app` собирает их через
`getAll()` и отдаёт в `ForteraNavHost`. Новый экран = `-api` с ключами + impl с провайдером.
`app/` трогать не надо.

### reduce vs updateState

`updateState { it.copy(...) }` — частичное обновление, в большинстве случаев. `reduce(newState)`
— когда новый стейт собран целиком. Оба доступны в `intent { }`. `setState` убрали.

### Ошибки

Репозитории возвращают `Result<T>`. `Interactor.execute { }` работает как `runCatching`, но
пробрасывает `CancellationException` — руками этот паттерн не повторяем. Непойманные
исключения из `intent { }` попадают в `MviViewModel.handleError`; переопредели на экране,
если нужен баннер или Crashlytics.

## Зависимости

- Фичи зависят от контрактов (`domain-*`), не от impl (`data-*`).
- Сид-фраза — только в `create-wallet`/`import-wallet` и в бридже. Балансы, отправка, адреса
  — через `wallet-balances`.
- Никаких `Dispatchers.IO/Main/Default` на местах — только `ForteraDispatchers.*`.
- `*-api` — листья графа, циклы невозможны.

## Именование

Папки и пути модулей — kebab-case: `:core:domain-crypto`, `:feature:create-wallet`. Пакеты
Kotlin — без разделителя: `com.yasashny.fortera.core.domaincrypto` (дефис в пакете нельзя,
отсюда склейка).

Конвеншн-плагины из `build-logic/`: `fortera.android.application(.compose)` на `:app`,
`fortera.android.library(.compose)` на библиотеки, `fortera.android.room` на Room-модули,
`fortera.android.feature` на impl-фичи (сам подтягивает core/ui+mvi+navigation+koin),
`kotlin.serialization` на `*-api` и `core/navigation`.

Раскладка фичи:

```
feature/foo-api/.../Foo.kt              @Serializable ключи
feature/foo/
  res/values/strings.xml                строки с префиксом foo_*
  presentation/  FooState|Intent|Effect|Reducer?|ViewModel.kt
  ui/            FooScreen.kt, FooLayout.kt, component/
  domain/        FooOverview.kt, FooOverviewInteractor.kt   (опц.)
  di/            FooModule.kt, FooNavProvider.kt
```

## Строки

Все пользовательские строки через `stringResource(R.string.foo_bar)`. Префикс = имя фичи
(snake_case); под-флоу группируются своим префиксом (`receive_*`, `send_*`, `confirm_send_*`).
Content description — суффикс `_cd`.

ViewModel не зовёт `stringResource`, поэтому ошибки отдаёт через `UiText`:

```kotlin
sendEffect(FooEffect.ShowError(UiText.of(R.string.foo_save_failed)))
...
is FooEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.asString())
```