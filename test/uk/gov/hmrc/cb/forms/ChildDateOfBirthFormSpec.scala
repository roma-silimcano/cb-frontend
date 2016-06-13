package uk.gov.hmrc.cb.forms

import java.time.LocalDate
import java.util.Calendar

import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 13/06/2016.
 */
class ChildDateOfBirthFormSpec extends UnitSpec with CBFakeApplication {

  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  "ChildDateOfBirthForm" should {

    "accept a valid date - today" in {
      val today = LocalDate.now()
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
        errors =>
          errors.errors shouldBe empty,
        success =>
          success shouldBe outputModel
      )
    }

    "accept a valid date - 20 years in the past" in {
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.YEAR, -20)

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe outputModel
        )
    }

    "throw a ValidationError - 20 years and 1 day in the past" in {
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.YEAR, -20)
      calendar.add(Calendar.DAY_OF_MONTH, -1)

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError - tomorrow" in {
      val calendar = Calendar.getInstance()
      calendar.add(Calendar.DAY_OF_MONTH, 1)

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for an incorrect day" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"32",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for an incorrect month" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val day = dateOfBirth.getDayOfMonth
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"13",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for an incorrect year" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"19999"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for special characters" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"@%",
          "dateOfBirth.month" -> s"&%",
          "dateOfBirth.year" -> s"*£"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError when value is negative" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"-10",
          "dateOfBirth.month" -> s"-12",
          "dateOfBirth.year" -> s"-2010"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError when empty" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  "",
          "dateOfBirth.month" -> "",
          "dateOfBirth.year" -> ""
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "Invalid",
          success =>
            success should not be outputModel
        )
    }

    "pre-populate the form with a value" in {
      val calendar = Calendar.getInstance()

      val today = calendar.getTime
      val dateOfBirth = formatter.parseDateTime(today.toString).toLocalDate

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = Some(dateOfBirth))
      val form = ChildDateOfBirthForm.form.fill(outputModel)
      form.get shouldBe outputModel
    }

  }

}
