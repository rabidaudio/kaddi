package audio.rabid.kadi

import audio.rabid.kadi.Example.Activity1
import audio.rabid.kadi.Example.Activity1ViewModel
import audio.rabid.kadi.Example.Activity2
import audio.rabid.kadi.Example.Activity2ViewModel
import audio.rabid.kadi.Example.AppModule
import audio.rabid.kadi.Example.Application
import audio.rabid.kadi.Example.Database
import audio.rabid.kadi.Example.Fragment
import audio.rabid.kadi.Example.FragmentViewModel
import audio.rabid.kadi.Example.IDatabase
import audio.rabid.kadi.Example.Logger
import audio.rabid.kadi.dsl.bind
import audio.rabid.kadi.dsl.module
import audio.rabid.kadi.dsl.toInstance
import com.winterbe.expekt.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class IntegrationSpec : Spek({

    beforeEachTest {
        Kadi.RootScope.close()
    }

    describe("Everything in root scope") {
        Kadi.RootScope.addModules(
            AppModule,
            module("Inline") {
                bind<Int>(0).toInstance(0)
            }
        )
        expect(Kadi.RootScope.get<Int>(0)).to.equal(0)
        val logger1 = Kadi.RootScope.get<Logger>()
        expect(logger1).to.be.an.instanceof(Logger::class.java)
        val logger2 = Kadi.RootScope.get<Logger>()
        expect(logger2).to.be.an.instanceof(Logger::class.java)
        expect(logger2).not.to.equal(logger1)
        val database1 = Kadi.RootScope.get<IDatabase>()
        expect(database1).to.be.an.instanceof(Database::class.java)
        val database2 = Kadi.RootScope.get<IDatabase>()
        expect(database2).to.be.an.instanceof(Database::class.java)
        expect(database2).to.equal(database1)

        Kadi.RootScope.addModules(module("Inline2") { bind<String>("foo").toInstance("bar") })
        expect(Kadi.RootScope.get<String>("foo")).to.equal("bar")
        expect(Kadi.RootScope.get<Int>(0)).to.equal(0)
    }

    describe("Dummy Application") {
        val application by memoized { Application() }
        val activity1 by memoized { Activity1(application) }
        val fragment1 by memoized { Fragment() }
        val activity2 by memoized { Activity2(application) }
        val fragment2 by memoized { Fragment() }

        it("should allow the creation of child scopes") {

            application.onApplicationCreate()
            val appScope = Kadi.getScope(application)
            expect(appScope.get<Logger>()).to.be.an.instanceof(Logger::class.java)
            expect(appScope.get<String>("AppName")).to.equal("MyApp")
            expectToThrow<IllegalStateException> { appScope.get<Activity1ViewModel>() }
            expectToThrow<IllegalStateException> { Kadi.RootScope.get<Logger>() }

            activity1.onCreate()

            expect(activity1.logger).to.be.an.instanceof(Logger::class.java)
            expect(activity1.viewModel).to.be.an.instanceof(Activity1ViewModel::class.java)
            expect(activity1.viewModel.appName).to.equal("MyApp")
            expect(activity1.viewModel.database).to.be.an.instanceof(Database::class.java)
            expect(activity1.viewModel.database).to.satisfy { it === appScope.get<IDatabase>() }

            fragment1.onAttach(activity1)
            expect(fragment1.viewModel).to.be.an.instanceof(FragmentViewModel::class.java)
            expect(Kadi.getScope(fragment1).get<Activity1ViewModel>()).to.satisfy { it === activity1.viewModel }
            expectToThrow<IllegalStateException> {
                Kadi.getScope(activity1).get<FragmentViewModel>()
            }
            expect(Kadi.getScope(fragment1).get<FragmentViewModel>()).to.equal(fragment1.viewModel)

            activity2.onCreate()
            fragment2.onAttach(activity2)

            expect(activity2.viewModel).to.be.an.instanceof(Activity2ViewModel::class.java)
            expect(fragment2.viewModel).to.be.an.instanceof(FragmentViewModel::class.java)
            expect(fragment2.viewModel).not.to.equal(fragment1.viewModel)
            expect(fragment1.viewModel.logger).not.to.satisfy { it === activity1.logger }

            fragment1.onDetach()

            expectToThrow<IllegalStateException> { Kadi.getScope(fragment1) }
            expect(Kadi.getScope(activity1).get<Activity1ViewModel>()).to.equal(activity1.viewModel)

            fragment2.onDetach()

            activity2.onDestroy()
            activity1.onDestroy()

            expectToThrow<IllegalStateException> { Kadi.getScope(fragment2) }

            val database = appScope.get<IDatabase>() as Database
            expect(database.isClosed).to.be.`false`
            Kadi.getScope(application).close()
            expect(database.isClosed).to.be.`true`
        }
    }
})
