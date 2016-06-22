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

import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.models.Child.{BirthCertificate, Name}

/**
  * Created by chrisianson on 26/05/16.
  */

object ChildrenManager {

  val childrenService = new ChildrenService

  class ChildrenService {

    private val START_INDEX = 1
    private val INCREMENT = 1

    def createListOfChildren(requiredNumberOfChildren: Int): List[Child] = {
      val children = for (i <- 1 to requiredNumberOfChildren) yield {
        val index = i
        Child(id = index.toShort)
      }
      children.toList
    }

    def modifyNumberOfChildren(requiredNumberOfChildren: Int, children: List[Child]): List[Child] = {
      val numberOfChildren = children.size
      val difference = requiredNumberOfChildren - numberOfChildren

      def modifyListOfChildrenHelper(children: List[Child], remaining: Int): List[Child] = {
        val sorted = children.sortBy(x => x.id)

        remaining match {
          case x if remaining == 0 =>
            // children stay the same
            sorted
          case x if remaining > 0 =>
            // add children
            val index = sorted match {
              case Nil =>
                START_INDEX
              case h :: t =>
                sorted.last.id + INCREMENT
            }

            val child = Child(id = index.toShort)
            val modified = child :: sorted
            modifyListOfChildrenHelper(modified, x - INCREMENT)
          case x if remaining < 0 =>
            // remove children
            val lastId = sorted.last.id - INCREMENT
            val modified = sorted.splitAt(lastId)._1
            modifyListOfChildrenHelper(modified, x + INCREMENT)
        }
      }

      if(difference == 0) {
        children
      } else {
        modifyListOfChildrenHelper(children, difference)
      }
    }

    def getChildById(index: Int, children: List[Child]): Option[Child] = {
      children.find(c => c.id == index)
    }

    def replaceChild(children: List[Child], index: Int, newChild: Child): List[Child] = {
      children.patch(index-1, Seq(newChild), 1)
    }

    def childExistsAtIndex(index: Int, children : List[Child]) = {
      val result = children.exists(x => x.id == index.toShort)
      Logger.debug(s"[ChildrenManager][childExistsAtIndex] $result $index")
      result
    }

    def addChild(id : Int, children : List[Child], newChild: Child) = {
      val amendedList = modifyNumberOfChildren(id, children)
      val modified = replaceChild(amendedList, id, newChild)
      modified
    }

    def create[T](id : Int, value : T) : Child = value match {
      case e : DateTime =>
        Child(id = id, dob = Some(e))
      case e : Name =>
        Child(id = id, firstname = Some(e._1), surname = Some(e._2))
      case e : BirthCertificate =>
        Child(id = id, birthCertificateReference = Some(e))
      case _ =>
        throw new IllegalArgumentException("I cannot crate a child with that type")
    }

    def edit[T](child : Child, change : T) = child.edit(change)

  }
}
