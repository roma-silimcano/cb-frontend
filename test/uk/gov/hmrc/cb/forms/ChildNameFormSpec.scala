package uk.gov.hmrc.cb.forms

import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 27/05/2016.
 */
class ChildNameFormSpec extends UnitSpec with WithFakeApplication {

  "ChildNameForm" should {

    /* valid */

    "accept a valid value for first name and last name" in {
      val data = ChildNamePageModel(firstName = Some("adam"), lastName = Some("conder"))
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "adam",
          "lastName" -> "conder"
        )
      ).fold(
          formWithErrors => {
            formWithErrors.errors shouldBe empty
          },
          success => {
            success shouldBe data
          }
        )
    }

    "accept a valid max length value for first name and last name" in {
      val data = ChildNamePageModel(
        firstName = Some("abcefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"),
        lastName = Some("abcefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
      )
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "abcefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
          "lastName" -> "abcefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        )
      ).fold(
          formWithErrors => {
            formWithErrors.errors shouldBe empty
          },
          success => {
            success shouldBe data
          }
        )
    }

    /* special */

    "throw a ValidationError when provided special characters for last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "adam",
          "lastName" = "@£$^%&&&&"
        ).fold(
         hasErrors =>
           hasErrors.errors.head.message shouldBe Messages("cb.error.child.last.name.invalid"),
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
        )
      )
    }

    "throw a ValidationError when provided special characters for first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "@%^&$@!",
          "lastName" = "Conder"
        ).fold(
            hasErrors =>
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid"),
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    "throw a ValidationError when provided special characters for last name and first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "@%^&$@!",
          "lastName" = "$%^@@@2"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    /* numbers */

    "throw a ValidationError when provided numbers for last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "Adam",
          "lastName" = "12345"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    "throw a ValidationError when provided numbers for first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "12345",
          "lastName" = "Conder"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    "throw a ValidationError when provided numbers for last name and first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "12345",
          "lastName" = "12345"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }


    /* empty */

    "throw a ValidationError for an empty first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "",
          "lastName" = "conder"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    "throw a ValidationError for an empty last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "adam",
          "lastName" = ""
        ).fold(
            hasErrors => {
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )

    }

    "throw a ValidationError for an empty first name and an empty last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "",
          "lastName" = ""
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    /* range */

    "throw a ValidationError for a value larger than max length for first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "B4lXqZv4jpFywUefGRmcK2aaiHaQ4h0SNIrFeLScWKh7yRwlttDcDattfvN7sS9NPx91Ba5LgmLzGJ5Lq8IsJzlUWp5YXmK2CBEJkwTgaLloPTi6pgbyPKQjH5G9DSTqs",
          "lastName" = "conder"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    "throw a ValidationError for a value larger than max length for last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "adam",
          "lastName" = "B4lXqZv4jpFywUefGRmcK2aaiHaQ4h0SNIrFeLScWKh7yRwlttDcDattfvN7sS9NPx91Ba5LgmLzGJ5Lq8IsJzlUWp5YXmK2CBEJkwTgaLloPTi6pgbyPKQjH5G9DSTqs"
        ).fold(
            hasErrors => {
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

    "throw a ValidationError for a value larger than max length for first name and last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" = "B4lXqZv4jpFywUefGRmcK2aaiHaQ4h0SNIrFeLScWKh7yRwlttDcDattfvN7sS9NPx91Ba5LgmLzGJ5Lq8IsJzlUWp5YXmK2CBEJkwTgaLloPTi6pgbyPKQjH5G9DSTqs",
          "lastName" = "B4lXqZv4jpFywUefGRmcK2aaiHaQ4h0SNIrFeLScWKh7yRwlttDcDattfvN7sS9NPx91Ba5LgmLzGJ5Lq8IsJzlUWp5YXmK2CBEJkwTgaLloPTi6pgbyPKQjH5G9DSTqs"
        ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
          )
      )
    }

  }

}
