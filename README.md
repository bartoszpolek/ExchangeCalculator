# Exchange

Exchange is a small Android currency calculator built with Kotlin, Jetpack Compose, Hilt,
Retrofit, and a modular project setup.

The app lets the user type a USDC amount or a selected fiat amount and calculates the other side
using the latest available rate. It also handles missing rates, network failures, loading states,
refresh, swap, and the unavailable currency-list API.

## Architecture

This project is more structured than a simple calculator needs to be. A single module would be
enough if the only goal was to build the smallest possible app.

I still chose a multi-module setup because this project is also a small showcase of how I would
organize a larger Android codebase. The architecture is strongly inspired by Google's Now in
Android project: feature-owned code, shared `core` modules, clear dependency direction, and Gradle
convention plugins in `build-logic`.

It is not a one-to-one copy of NIA, because this app is much smaller, but it follows the same ideas
where they make sense:

- `:app` wires the application and dependency graph.
- `:feature:exchange` contains the exchange feature.
- `:core:common` contains shared non-UI contracts like formatting and typed results.
- `:core:network` contains shared network setup and error mapping.
- `:core:designsystem` contains Compose components, theme, and UI tokens.
- `:build-logic` keeps Gradle setup consistent across modules.

Inside `:feature:exchange`, the code is split into:

- `data` - DTOs, Retrofit data sources, repository implementations, and DI bindings.
- `domain` - models, repository contracts, and calculation use cases.
- `presentation` - screen state, ViewModel, composables, and UI-specific logic.

Some of this is definitely overengineering for the current app size. That was a conscious choice:
I wanted to keep the project realistic for future growth and show that I can work with modular
Android architecture, not only with a single-screen Compose setup.

## Currency List Fallback

The task says:

```text
The app must be fully functional despite this API not being available yet.
Handle this scenario appropriately.
```

I treated the currency-list API as optional from the app's point of view. If the remote endpoint
works, the app uses it. If it is not implemented yet, fails, or returns an empty list, the app falls
back to a static list of supported currencies.

That keeps the main flow usable: the user can still open the app, choose a currency, fetch rates,
enter amounts, swap fields, and refresh. The missing currency-list service does not block the app.

I also let unknown remote currency codes pass through when the API does return data. That way the
app is not hardcoded to reject new currencies as soon as the backend starts returning them.

## Mid-rate Decision

The API gives ask and bid values. The app uses a mid-rate:

```text
midRate = (ask + bid) / 2
```

I picked this because the current product is a calculator, not a buy/sell flow. There is no UI
where the user chooses whether they are buying or selling, so using ask in one direction and bid in
the other would make swapped calculations return different values.

That difference can be correct from a market perspective, but in this UI it would be easy to
misread. If we wanted to use side-specific prices even in a calculator, I would adjust the UI to
explain where the difference comes from: show ask/bid, label which rate is used in each direction,
and make it clear that swapping the fields changes the pricing side.

For this version, mid-rate keeps the calculator predictable and avoids surprising the user with a
difference that the interface does not explain.

## UI and Error Handling

The UI keeps the calculator stable and shows errors close to the thing that failed:

- if a selected currency pair has no available rate, the rate area shows a rate-unavailable message,
- if refresh fails because of network issues, the app can keep the last rate visible and show a top
  banner,
- while loading, the amount fields stay visible but disabled, so the layout does not jump around.

## Testing

The project has tests at a few levels:

- formatter and amount-input normalization tests,
- network error mapping tests,
- DTO mapper tests,
- repository tests with fakes,
- Retrofit data source tests with MockWebServer,
- domain use case tests,
- ViewModel tests with fake repositories,
- Compose UI instrumentation tests for the exchange screen.

The Compose UI tests use a small robot helper. It keeps the tests focused on user intent, instead
of repeating `composeRule.onNode...` selectors in every test.
