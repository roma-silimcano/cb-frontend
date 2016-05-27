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

import uk.gov.hmrc.cb.mappings.Genders
import uk.gov.hmrc.cb.models.Child

/**
  * Created by chrisianson on 26/05/16.
  */

trait ChildrenManager {

  val childrenService = new ChildrenService

  class ChildrenService {

    private def createChild(index: Int) = {
      Child(
        id = index.toShort,
        uniqueReferenceNumber = None,
        firstname = None,
        surname = None,
        dob = None,
        gender = Genders.Male,
        previousClaim = false
      )
    }

    def createListOfChildren(requiredNumberOfChildren: Int): List[Child] = {
      val children = for (i <- 1 to requiredNumberOfChildren) yield {
        val index = i
        val child = createChild(index)
        child
      }
      children.toList
    }

    def modifyListOfChildren(requiredNumberOfChildren: Int, children: List[Child]): List[Child] = {
      val numberOfChildren = children.size
      val difference = requiredNumberOfChildren - numberOfChildren

      def modifyListOfChildrenHelper(children: List[Child], remaining: Int): List[Child] = {

        val sorted = children.sortBy(x => x.id)

        remaining match {
          case x if remaining == 0 =>
            sorted
          case x if remaining > 0 =>
            val index = sorted.last.id + 1
            val child = createChild(index)
            val modified = child :: sorted
            modifyListOfChildrenHelper(modified, x - 1)
          case x if remaining < 0 =>
            val lastId = sorted.last.id - 1
            val modified = sorted.splitAt(lastId)._1
            modifyListOfChildrenHelper(modified, x + 1)
        }
      }

      if(difference == 0) {
        children
      } else {
        modifyListOfChildrenHelper(children, difference)
      }
    }

    def getChildById(index: Int, children: List[Child]): Child = {
      try {
        val child = children.filter(c => c.id == index.toShort).head
        child
      }
      catch {
        case e : Exception => throw e
      }
    }

    def replaceChildInAList(children: List[Child], index: Int, newChild: Child): List[Child] = {
      children.patch(index-1, Seq(newChild), 1)
    }
  }
}
