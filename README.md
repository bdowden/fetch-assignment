# Fetch Assignment
This application retrieves data from an endpoint hosted on AWS in json format. It then filters invalid data (`null` or empty `name` field) which then is displayed on the screen. Data is grouped by `listId` and sorted, then sorted by `name` within the grouping.

Jetpack compose is used as the frontend stack.

## Libraries Used
* Retrofit - For calling HTTP endpoints
* GSON - For deserializing json into Kotlin objects
* Dagger/Hilt - dependency injection/IoC to allow isolated unit testing
* JUnit - For unit testing
* mockk - For mocking interfaces in unit tests
* Kotlinx-datetime - Using the `Instant` datatype for cache expiration

## Architecture
The app uses a standard architecture: a service to retrieve data, a repository to call the service or retrieve results from cache, a view model to perform logic around the data, view state to represent the content on the screen, and the view itself.

I used an instance variable to cache the result; ideally this would be done with a caching object, utilizing a local data store such as Room. I didn't go with this approach as it is a single API call. With the use of DI/IoC it is straightfoward to modify the code to add that feature.

## UI
I am the first to admit that my UI/UX creativity is not great; I can replicate what a designer gives me but coming up with something on my own is not my strength.
I created different states for the UI - `Loading`, `Error`, `No Results`, and `Success`. Each of these states appear at their respective times and display the expected elements. 
Sticky Headers are used to show the `listId` grouping, then simple labels for `id` and `name`.

## Workarounds
I had one hiccup with this code - using `kotlin.Result<T>` caused issues with mockk in my unit tests. For whatever reason when mocking the api service call the mmocked result ended up getting wrapped in a `Result<T>`, creating `Result<Result<List<FetchItem>>`. A quick search showed similar things back in 2020 that should have been resolved but it wasn't. Instead of fighting with it I decided to create my own `Result<T>` with the functions I needed and my tests passed.
