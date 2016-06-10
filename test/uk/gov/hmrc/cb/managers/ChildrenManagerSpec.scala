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
class ChildrenManagerSpec extends UnitSpec {

  def fixture = new {
    val child1 = Some(Child(1, birthCertificateReference = None, firstname = None, surname = None, dob = None, gender = Genders.None, previousClaim = false))
    val child2 = Some(Child(2, birthCertificateReference = None, firstname = None, surname = None, dob = None, gender = Genders.None, previousClaim = false))
    val child3 = Some(Child(3, birthCertificateReference = None, firstname = None, surname = None, dob = None, gender = Genders.None, previousClaim = false))
    val child4 = Some(Child(4, birthCertificateReference = None, firstname = None, surname = None, dob = None, gender = Genders.None, previousClaim = false))
    val child5 = Some(Child(5, birthCertificateReference = None, firstname = None, surname = None, dob = None, gender = Genders.None, previousClaim = false))
    val replacementChild1 = Some(Child(2, birthCertificateReference = None, firstname = Some("Ricky"), surname = Some("Hatton"), dob = Some(LocalDate.now()), gender = Genders.None, previousClaim = false))
    val replacementChild2 = Some(Child(2, birthCertificateReference = None, firstname = Some("Frank"), surname = Some("Bruno"), dob = Some(LocalDate.now()), gender = Genders.None, previousClaim = false))
  }

  "ChildrenManager" when {

    "creating children" should {

      "create a list of children objects" in {
        val result = ChildrenManager.childrenService.createListOfChildren(requiredNumberOfChildren = 1)
        result shouldBe List(
          fixture.child1.get
        )
      }

      "return an empty List when provided 0" in {
        val result = ChildrenManager.childrenService.createListOfChildren(requiredNumberOfChildren = 0)
        result shouldBe List.empty
      }

      "create a list of 5 child objects when passed 5" in {
        val result = ChildrenManager.childrenService.createListOfChildren(requiredNumberOfChildren = 5)
        result shouldBe List(
          fixture.child1.get,
          fixture.child2.get,
          fixture.child3.get,
          fixture.child4.get,
          fixture.child5.get
        )
        result.length shouldBe 5
      }
    }

    "modifying children" should {

      "(remove) a list of children objects" in {
        val input = List(
          fixture.child1.get,
          fixture.child2.get
        )
        val result = ChildrenManager.childrenService.modifyNumberOfChildren(requiredNumberOfChildren = 1, children = input)
        result shouldBe List(
          fixture.child1.get
        )
        result.length shouldBe 1
      }

      "(remove) drop multiple children objects" in {
        val input = List(
          fixture.child1.get,
          fixture.child2.get,
          fixture.child3.get,
          fixture.child4.get
        )
        val result = ChildrenManager.childrenService.modifyNumberOfChildren(requiredNumberOfChildren = 2, children = input)
        result shouldBe List(
          fixture.child1.get,
          fixture.child2.get
        )
        result.length shouldBe 2
      }

      "(add) modify existing list of children by adding 2 more children" in {
        val input = List(
          fixture.child1.get
        )
        val result = ChildrenManager.childrenService.modifyNumberOfChildren(requiredNumberOfChildren = 3, children = input)
        result shouldBe List(
          fixture.child1.get,
          fixture.child2.get,
          fixture.child3.get
        )
        result.length shouldBe 3
      }

      "(same) modify a list of children objects" in {
        val input = List(
          fixture.replacementChild1.get,
          fixture.replacementChild2.get
        )
        val result = ChildrenManager.childrenService.modifyNumberOfChildren(requiredNumberOfChildren = 2, children = input)
        result shouldBe List(
          fixture.replacementChild1.get,
          fixture.replacementChild2.get
        )
        result.length shouldBe 2
      }
    }

    "get childById" should {

      "return a child by id when an index exists" in {
        val children = ChildrenManager.childrenService.createListOfChildren(5)
        val result = ChildrenManager.childrenService.getChildById(3, children)
        result shouldBe fixture.child3
      }

      "return last child by id" in {
        val children = ChildrenManager.childrenService.createListOfChildren(5)
        val result = ChildrenManager.childrenService.getChildById(5, children)

        result shouldBe fixture.child5
      }

      "return None when an id is greater than length of list" in {
        val children = ChildrenManager.childrenService.createListOfChildren(2)
        val result = ChildrenManager.childrenService.getChildById(5, children)
        result shouldBe None
      }

      "return None when id is 0" in {
        val children = ChildrenManager.childrenService.createListOfChildren(requiredNumberOfChildren = 3)
        val result = ChildrenManager.childrenService.getChildById(0, children)
        result shouldBe None
      }

      "return None when child list is empty" in {
        val result = ChildrenManager.childrenService.getChildById(1, Nil)
        result shouldBe None
      }
    }

    "performing a lookup" should {

      "determine if a child exists by id - true" in {
        val children = ChildrenManager.childrenService.createListOfChildren(5)
        val result = ChildrenManager.childrenService.childExistsAtIndex(3, children)
        result shouldBe true
      }

      "determine if a child exists by id - false" in {
        val children = ChildrenManager.childrenService.createListOfChildren(5)
        val result = ChildrenManager.childrenService.childExistsAtIndex(6, children)
        result shouldBe false
      }

    }

    "replacing child in a list" in {
      val childList = List(
        fixture.child1.get,
        fixture.child2.get
      )
      val modifiedChild = fixture.replacementChild2.get
      val result = ChildrenManager.childrenService.replaceChild(childList, 2, modifiedChild)
      result shouldBe List(
        fixture.child1.get,
        fixture.replacementChild2.get
      )
      result.length shouldBe 2
    }
  }
}
