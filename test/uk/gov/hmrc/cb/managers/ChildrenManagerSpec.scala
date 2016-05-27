/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cb.managers

import java.util.NoSuchElementException

import org.joda.time.LocalDate
import uk.gov.hmrc.cb.mappings.Genders
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 26/05/16.
  */
class ChildrenManagerSpec extends UnitSpec with ChildrenManager {

  def fixture = new {
    val child1 = Child(1, uniqueReferenceNumber = None, firstname = None, surname = None, dob = None, gender = Genders.Male, previousClaim = false)
    val child2 = Child(2, uniqueReferenceNumber = None, firstname = None, surname = None, dob = None, gender = Genders.Male, previousClaim = false)
    val child3 = Child(3, uniqueReferenceNumber = None, firstname = None, surname = None, dob = None, gender = Genders.Male, previousClaim = false)
    val child4 = Child(4, uniqueReferenceNumber = None, firstname = None, surname = None, dob = None, gender = Genders.Male, previousClaim = false)
    val child5 = Child(5, uniqueReferenceNumber = None, firstname = None, surname = None, dob = None, gender = Genders.Male, previousClaim = false)
    val replacementChild1 = Child(2, uniqueReferenceNumber = None, firstname = Some("Ricky"), surname = Some("Hatton"), dob = Some(LocalDate.now()), gender = Genders.Male, previousClaim = false)
    val replacementChild2 = Child(2, uniqueReferenceNumber = None, firstname = Some("Frank"), surname = Some("Bruno"), dob = Some(LocalDate.now()), gender = Genders.Male, previousClaim = false)
  }

  "ChildrenManager" when {

    "creating children" should {

      "create a list of children objects" in {
        val result = childrenService.createListOfChildren(requiredNumberOfChildren = 1)
        result shouldBe List(
          fixture.child1
        )
      }

      "return an empty List when provided 0" in {
        val result = childrenService.createListOfChildren(requiredNumberOfChildren = 0)
        result shouldBe List.empty
      }

      "create a list of 5 child objects when passed 5" in {
        val result = childrenService.createListOfChildren(requiredNumberOfChildren = 5)
        result shouldBe List(
          fixture.child1,
          fixture.child2,
          fixture.child3,
          fixture.child4,
          fixture.child5
        )
        result.length shouldBe 5
      }
    }

    "modifying children" should {

      "(remove) a list of children objects" in {
        val input = List(
          fixture.child1,
          fixture.child2
        )
        val result = childrenService.modifyListOfChildren(requiredNumberOfChildren = 1, children = input)
        result shouldBe List(
          fixture.child1
        )
        result.length shouldBe 1
      }

      "(remove) drop multiple children objects" in {
        val input = List(
          fixture.child1,
          fixture.child2,
          fixture.child3,
          fixture.child4
        )
        val result = childrenService.modifyListOfChildren(requiredNumberOfChildren = 2, children = input)
        result shouldBe List(
          fixture.child1,
          fixture.child2
        )
        result.length shouldBe 2
      }

      "(add) modify existing list of children by adding 2 more children" in {
        val input = List(
          fixture.child1
        )
        val result = childrenService.modifyListOfChildren(requiredNumberOfChildren = 3, children = input)
        result shouldBe List(
          fixture.child1,
          fixture.child2,
          fixture.child3
        )
        result.length shouldBe 3
      }

      "(same) modify a list of children objects" in {
        val input = List(
          fixture.replacementChild1,
          fixture.replacementChild2
        )
        val result = childrenService.modifyListOfChildren(requiredNumberOfChildren = 2, children = input)
        result shouldBe List(
          fixture.replacementChild1,
          fixture.replacementChild2
        )
        result.length shouldBe 2
      }
    }

    "get childById" should {

      "return a child by id when an index exists" in {
        val children = childrenService.createListOfChildren(5)
        val result = childrenService.getChildById(3, children)
        result shouldBe fixture.child3
      }

      "return last child by id" in {
        val children = childrenService.createListOfChildren(5)
        val result = childrenService.getChildById(5, children)

        result shouldBe fixture.child5
      }

      "return an exception when an id is greater than length of list" in {
        val result = intercept[NoSuchElementException] {
          val children = childrenService.createListOfChildren(2)
          childrenService.getChildById(5, children)
        }
        result shouldBe a[NoSuchElementException]
      }

      "return exception when id is 0" in {
        val result = intercept[NoSuchElementException] {
          val children = childrenService.createListOfChildren(requiredNumberOfChildren = 3)
          childrenService.getChildById(0, children)
        }
        result shouldBe a[NoSuchElementException]
      }

      "return exception when child list is empty" in {
        val result = intercept[Exception] {
          childrenService.getChildById(1, List())
        }
        result shouldBe a[Exception]
      }
    }

    "replacing child in a list" in {
      val childList = List(
        fixture.child1,
        fixture.child2
      )
      val modifiedChild = fixture.replacementChild2
      val result = childrenService.replaceChildInAList(childList, 2, modifiedChild)
      result shouldBe List(
        fixture.child1,
    fixture.replacementChild2
      )
      result.length shouldBe 2
    }
  }
}
