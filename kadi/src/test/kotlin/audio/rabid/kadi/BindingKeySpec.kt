package audio.rabid.kadi

import com.winterbe.expekt.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class BindingKeySpec : Spek({

    describe("BindingKey") {

        describe("equality") {

            it("should be equal for the same class") {
                expect(BindingKey(String::class)).to.equal(BindingKey(String::class))
            }

            it("should be equal for the same class and identifier") {
                expect(BindingKey(String::class, "Foo")).to.equal(BindingKey(String::class, "Foo"))
            }

            it("should not be equal for a different class") {
                val a: BindingKey<*> = BindingKey(String::class)
                val b: BindingKey<*> = BindingKey(Int::class)
                expect(a).not.to.equal(b)
            }

            it("should not be equal for a different identifier") {
                expect(BindingKey(String::class, "Foo")).not.to.equal(BindingKey(String::class, "Bar"))
            }

            it("should not be equal for a sub-class") {
                val a: BindingKey<*> = BindingKey(String::class)
                val b: BindingKey<*> = BindingKey(CharSequence::class)
                expect(a).not.to.equal(b)
                expect(b).not.to.equal(a)
            }
        }
    }
})
