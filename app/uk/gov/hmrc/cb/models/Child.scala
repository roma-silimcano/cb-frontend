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

package uk.gov.hmrc.cb.models

import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.cb.mappings.Genders

/**
  * Created by chrisianson on 26/05/16.
  */


case class Child (
                 id: Short,
                 birthCertificateReference: Option[String] = None,
                 firstname: Option[String] = None,
                 surname: Option[String] = None,
                 dob: Option[LocalDate] = None,
                 gender: Genders.Gender = Genders.None,
                 previousClaim: Boolean = false
                 ) {

  def edit(birthCertificateReference: String) = copy(birthCertificateReference = Some(birthCertificateReference))
  def edit(firstName : String, surname: String) =  copy(firstname = Some(firstName), surname = Some(surname))

  def hasBirthCertificateReferenceNumber : Boolean = birthCertificateReference.isDefined

}

object Child {

//  type BirthNumber = String
//  type Name = String

//  import play.api.libs.functional.syntax._ // Combinator syntax

//  implicit val birthNumberFormat : Format[BirthNumber] = Format.of[String]
//  implicit val nameFormat : Format[Name] = Format.of[String]
  implicit val formats = Json.format[Child]

//  val childReads: Reads[Child] = (
//    (JsPath \ "id").read[Short] and
//      (JsPath \ "birthCertificateReference").read[String] and
//        (JsPath \ "firstname").read[Option[String]] and
//          (JsPath \ "surname").read[Option[String]] and
//            (JsPath \ "dob").read[Option[LocalDate]] and
//              (JsPath \ "gender").read[Genders.Gender] and
//                (JsPath \ "previousClaim").read[Boolean]
//    )(Child.apply _)
//
//  val childWrites: Writes[Child] = (
//    (JsPath \ "id").write[Short] and
//      (JsPath \ "birthCertificateReference").write[String] and
//      (JsPath \ "firstname").write[Option[String]] and
//      (JsPath \ "surname").write[Option[String]] and
//      (JsPath \ "dob").write[Option[LocalDate]] and
//      (JsPath \ "gender").write[Genders.Gender] and
//      (JsPath \ "previousClaim").write[Boolean]
//    )(unlift(Child.unapply))
//
//  implicit val locationFormat: Format[Child] =
//    Format(childReads, childWrites)

}
