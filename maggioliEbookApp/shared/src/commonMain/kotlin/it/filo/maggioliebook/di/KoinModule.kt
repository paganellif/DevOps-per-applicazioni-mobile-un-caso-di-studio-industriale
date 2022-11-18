package it.filo.maggioliebook.di

import it.filo.maggioliebook.datasource.local.BookmarkDataSource
import it.filo.maggioliebook.datasource.local.FavoriteDataSource
import it.filo.maggioliebook.datasource.local.HighlightDataSource
import it.filo.maggioliebook.datasource.local.ProgressionDataSource
import it.filo.maggioliebook.datasource.remote.user.LoginDataSource
import it.filo.maggioliebook.datasource.remote.user.UserDataSource
import it.filo.maggioliebook.repository.core.AutoreRepository
import it.filo.maggioliebook.repository.core.FascicoloRepository
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.repository.core.RivistaRepository
import it.filo.maggioliebook.repository.user.UserRepository
import it.filo.maggioliebook.datasource.remote.core.AutoreDataSource
import it.filo.maggioliebook.datasource.remote.core.FascicoloDataSource
import it.filo.maggioliebook.datasource.remote.core.LibroDataSource
import it.filo.maggioliebook.datasource.remote.core.PaginaLibroDataSource
import it.filo.maggioliebook.datasource.remote.util.Pdf2EpubService
import it.filo.maggioliebook.datasource.remote.core.RivistaDataSource
import it.filo.maggioliebook.db.MaggioliEbookDB
import it.filo.maggioliebook.usecase.core.ConvertPdf2EpubUseCase
import it.filo.maggioliebook.usecase.core.GetBookCoverUseCase
import it.filo.maggioliebook.usecase.user.CheckUserLoggedUseCase
import it.filo.maggioliebook.usecase.core.GetBookMetadataUseCase
import it.filo.maggioliebook.usecase.core.SearchBookUseCase
import it.filo.maggioliebook.usecase.core.bookmark.AddBookmarkForBookUseCase
import it.filo.maggioliebook.usecase.core.bookmark.GetBookBookmarksUseCase
import it.filo.maggioliebook.usecase.core.bookmark.RemoveBookmarkForBookUseCase
import it.filo.maggioliebook.usecase.core.favorite.GetAllFavoriteBooksUseCase
import it.filo.maggioliebook.usecase.core.favorite.IsBookFavoriteUseCase
import it.filo.maggioliebook.usecase.core.favorite.SetBookAsFavoriteUseCase
import it.filo.maggioliebook.usecase.core.favorite.UnsetBookAsFavoriteUseCase
import it.filo.maggioliebook.usecase.core.highlight.AddHighlightUseCase
import it.filo.maggioliebook.usecase.core.highlight.GetBookHighlightsUseCase
import it.filo.maggioliebook.usecase.core.highlight.GetHighlightUseCase
import it.filo.maggioliebook.usecase.core.highlight.RemoveHighlightUseCase
import it.filo.maggioliebook.usecase.core.progression.AddBookProgressionUseCase
import it.filo.maggioliebook.usecase.core.progression.GetBookProgressionUseCase
import it.filo.maggioliebook.usecase.user.GetUserInfoUseCase
import it.filo.maggioliebook.usecase.user.UserLoginUseCase
import it.filo.maggioliebook.usecase.user.UserLogoutUseCase
import it.filo.maggioliebook.util.JwtManager
import it.filo.maggioliebook.util.extensions.ByteArrayConverter
import it.filo.maggioliebook.util.extensions.createDriver
import it.filo.maggioliebook.util.getDispatcherProvider
import it.filo.maggioliebook.util.getKVaultProvider
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val utilModule = module {
    factory { getDispatcherProvider() }
    single { JwtManager(getKVaultProvider().kvault) }
    single { MaggioliEbookDB(createDriver("maggioliebook.db")) }
    factory { ByteArrayConverter() }
}

private val apiModule = module {
    single { LoginDataSource(get()) }
    single { AutoreDataSource(get()) }
    single { FascicoloDataSource(get()) }
    single { LibroDataSource(get()) }
    single { PaginaLibroDataSource(get()) }
    single { RivistaDataSource(get()) }
    single { UserDataSource(get()) }
    single { Pdf2EpubService(get()) }
    single { FavoriteDataSource(get(), get()) }
    single { HighlightDataSource(get(), get()) }
    single { BookmarkDataSource(get(), get()) }
    single { ProgressionDataSource(get(), get()) }
}

private val repositoryModule = module {
    single { AutoreRepository() }
    single { FascicoloRepository() }
    single { LibroRepository() }
    single { RivistaRepository() }
    single { UserRepository() }
}

private val useCaseModule = module {
    // CORE USE CASE
    factory { GetBookMetadataUseCase() }
    factory { SearchBookUseCase() }
    factory { ConvertPdf2EpubUseCase() }
    factory { GetBookCoverUseCase() }

    // favorite use case
    factory { IsBookFavoriteUseCase() }
    factory { SetBookAsFavoriteUseCase() }
    factory { UnsetBookAsFavoriteUseCase() }
    factory { GetAllFavoriteBooksUseCase() }

    // bookmark use case
    factory { AddBookmarkForBookUseCase() }
    factory { RemoveBookmarkForBookUseCase() }
    factory { GetBookBookmarksUseCase() }

    // highlight use case
    factory { GetBookHighlightsUseCase() }
    factory { GetHighlightUseCase() }
    factory { RemoveHighlightUseCase() }
    factory { AddHighlightUseCase() }

    // progression use case
    factory { AddBookProgressionUseCase() }
    factory { GetBookProgressionUseCase() }

    // USER USE CASE
    factory { CheckUserLoggedUseCase() }
    factory { UserLoginUseCase() }
    factory { UserLogoutUseCase() }
    factory { GetUserInfoUseCase() }
}

private val sharedModule = listOf(utilModule, useCaseModule, repositoryModule, apiModule,)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(sharedModule)
}

fun initKoin() = initKoin {}