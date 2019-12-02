package audio.rabid.kaddi

import audio.rabid.kaddi.Example.ALoggerEndpoint
import audio.rabid.kaddi.Example.Activity1
import audio.rabid.kaddi.Example.Activity1ViewModel
import audio.rabid.kaddi.Example.Activity2
import audio.rabid.kaddi.Example.Activity2ViewModel
import audio.rabid.kaddi.Example.AppModule
import audio.rabid.kaddi.Example.Application
import audio.rabid.kaddi.Example.Database
import audio.rabid.kaddi.Example.Fragment
import audio.rabid.kaddi.Example.FragmentViewModel
import audio.rabid.kaddi.Example.IDatabase
import audio.rabid.kaddi.Example.Logger
import audio.rabid.kaddi.Example.LoggerEndpoint
import audio.rabid.kaddi.dsl.bind
import audio.rabid.kaddi.dsl.module
import audio.rabid.kaddi.dsl.toInstance
import com.winterbe.expekt.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class IntegrationSpec : Spek({

    beforeEachTest {
        Kaddi.RootScope.close()
    }

    describe("Everything in root scope") {

        it("should allow modules to be added on the fly") {
            Kaddi.RootScope.addModules(
                    AppModule,
                    module("Inline") {
                        bind<Int>(0).toInstance(0)
                    }
            )
            expect(Kaddi.RootScope.getInstance<Int>(0)).to.equal(0)
            val logger1 = Kaddi.RootScope.getInstance<Logger>()
            expect(logger1).to.be.an.instanceof(Logger::class.java)
            val logger2 = Kaddi.RootScope.getInstance<Logger>()
            expect(logger2).to.be.an.instanceof(Logger::class.java)
            expect(logger2).not.to.equal(logger1)
            val database1 = Kaddi.RootScope.getInstance<IDatabase>()
            expect(database1).to.be.an.instanceof(Database::class.java)
            val database2 = Kaddi.RootScope.getInstance<IDatabase>()
            expect(database2).to.be.an.instanceof(Database::class.java)
            expect(database2).to.equal(database1)

            val loggerEndpoints = Kaddi.RootScope.getSetInstance<LoggerEndpoint>()
            expect(loggerEndpoints.size).to.equal(1)
            expect(loggerEndpoints.first()).to.be.an.instanceof(ALoggerEndpoint::class.java)
            // Because the set binding entry for ALoggerEndpoint is not a singleton, Logger should have gotten
            // a different set than we just did
            expect(loggerEndpoints.first()).not.to.equal(logger1.endpoints.first())

            Kaddi.RootScope.addModules(module("Inline2") { bind<String>("foo").toInstance("bar") })
            expect(Kaddi.RootScope.getInstance<String>("foo")).to.equal("bar")
            expect(Kaddi.RootScope.getInstance<Int>(0)).to.equal(0)
        }
    }

    describe("Dummy scoped Application") {
        val application by memoized { Application() }
        val activity1 by memoized { Activity1(application) }
        val fragment1 by memoized { Fragment() }
        val activity2 by memoized { Activity2(application) }
        val fragment2 by memoized { Fragment() }

        it("should allow the creation of child scopes") {

            application.onApplicationCreate()
            val appScope = Kaddi.getScope(application)
            expect(appScope.getInstance<Logger>()).to.be.an.instanceof(Logger::class.java)
            expect(appScope.getInstance<String>("AppName")).to.equal("MyApp")
            expectToThrow<IllegalStateException> { appScope.getInstance<Activity1ViewModel>() }
            expectToThrow<IllegalStateException> { Kaddi.RootScope.getInstance<Logger>() }

            activity1.onCreate()

            expect(activity1.logger).to.be.an.instanceof(Logger::class.java)
            expect(activity1.viewModel).to.be.an.instanceof(Activity1ViewModel::class.java)
            expect(activity1.viewModel.appName).to.equal("MyApp")
            expect(activity1.viewModel.database).to.be.an.instanceof(Database::class.java)
            expect(activity1.viewModel.database).to.satisfy { it === appScope.getInstance<IDatabase>() }

            fragment1.onAttach(activity1)
            expect(fragment1.viewModel).to.be.an.instanceof(FragmentViewModel::class.java)
            expect(Kaddi.getScope(fragment1).getInstance<Activity1ViewModel>()).to.satisfy { it === activity1.viewModel }
            expectToThrow<IllegalStateException> {
                Kaddi.getScope(activity1).getInstance<FragmentViewModel>()
            }
            expect(Kaddi.getScope(fragment1).getInstance<FragmentViewModel>()).to.equal(fragment1.viewModel)

            activity2.onCreate()
            fragment2.onAttach(activity2)

            expect(activity2.viewModel).to.be.an.instanceof(Activity2ViewModel::class.java)
            expect(fragment2.viewModel).to.be.an.instanceof(FragmentViewModel::class.java)
            expect(fragment2.viewModel).not.to.equal(fragment1.viewModel)
            expect(fragment1.viewModel.logger).not.to.satisfy { it === activity1.logger }

            fragment1.onDetach()

            expectToThrow<IllegalStateException> { Kaddi.getScope(fragment1) }
            expect(Kaddi.getScope(activity1).getInstance<Activity1ViewModel>()).to.equal(activity1.viewModel)

            fragment2.onDetach()

            activity2.onDestroy()
            activity1.onDestroy()

            expectToThrow<IllegalStateException> { Kaddi.getScope(fragment2) }

            val database = appScope.getInstance<IDatabase>() as Database
            expect(database.isClosed).to.be.`false`
            Kaddi.getScope(application).close()
            expect(database.isClosed).to.be.`true`
        }
    }
})
