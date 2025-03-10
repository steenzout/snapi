/*
 * Copyright 2023 RAW Labs S.A.
 *
 * Use of this software is governed by the Business Source License
 * included in the file licenses/BSL.txt.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0, included in the file
 * licenses/APL.txt.
 */

package com.rawlabs.snapi.compiler.tests.output

import com.rawlabs.utils.core.RawUtils
import com.rawlabs.snapi.compiler.tests.Rql2TestContext
import com.rawlabs.snapi.frontend.rql2._
import com.rawlabs.utils.core._

import java.nio.file.Files

class CsvOutputTest extends Rql2TestContext {

  option("output-format", "csv")

  private val csvWithAllTypes = tempFile(
    """byteCol;shortCol;intCol;longCol;floatCol;doubleCol;decimalCol;boolCol;nullBoolCol;dateCol;timeCol;timestampCol
      |1;10;100;1000;3.14;6.28;9.42;true;false;2023-12-25;01:02:03;2023-12-25T01:02:03
      |120;2500;25000;9223372036854775807;30.14;60.28;90.42;false;;2023-02-05;11:12:13;2023-02-05T11:12:13""".stripMargin
  )

  test("""[
    |{byteCol: Int.From("1"), shortCol:Int.From("10"), intCol: Int.From("100"), longCol: Int.From("1000"),
    | floatCol: Double.From("3.14"), doubleCol: Double.From("6.28"), decimalCol: Decimal.From("9.42"), boolCol: true,
    | dateCol: Date.Parse("12/25/2023", "M/d/yyyy"), timeCol: Time.Parse("01:02:03", "H:m:s"),
    | timestampCol: Timestamp.Parse("12/25/2023 01:02:03", "M/d/yyyy H:m:s"), binaryCol: Binary.FromString("Hello World!"),
    | stringCol:"Hello,World!"},
    |{byteCol: Int.From("120"), shortCol:Int.From("2500"), intCol: Int.From("25000"), longCol: Long.From("9223372036854775807"),
    | floatCol: Double.From("30.14"), doubleCol: Double.From("60.28"), decimalCol: Decimal.From("90.42"), boolCol: false,
    | dateCol: Date.Parse("2/5/2023", "M/d/yyyy"), timeCol: Time.Parse("11:12:13", "H:m:s"),
    | timestampCol: Timestamp.Parse("2/5/2023 11:12:13", "M/d/yyyy H:m:s"), binaryCol: Binary.FromString("Olala!"),
    | stringCol:"Ciao World!"}
    |]""".stripMargin) { it =>
    val path = Files.createTempFile("", "")
    try {
      it should saveToInFormat(path, "csv")
      path should contain(
        """byteCol,shortCol,intCol,longCol,floatCol,doubleCol,decimalCol,boolCol,dateCol,timeCol,timestampCol,binaryCol,stringCol
          |1,10,100,1000,3.14,6.28,9.42,true,2023-12-25,01:02:03,2023-12-25T01:02:03,SGVsbG8gV29ybGQh,"Hello,World!"
          |120,2500,25000,9223372036854775807,30.14,60.28,90.42,false,2023-02-05,11:12:13,2023-02-05T11:12:13,T2xhbGEh,Ciao World!
          |""".stripMargin
      )
    } finally {
      RawUtils.deleteTestPath(path)
    }
  }

  test(snapi"""Csv.InferAndRead("$csvWithAllTypes")""") { it =>
    val path = Files.createTempFile("", "")
    try {
      it should saveToInFormat(path, "csv")
      path should contain(
        """byteCol,shortCol,intCol,longCol,floatCol,doubleCol,decimalCol,boolCol,nullBoolCol,dateCol,timeCol,timestampCol
          |1,10,100,1000,3.14,6.28,9.42,true,false,2023-12-25,01:02:03,2023-12-25T01:02:03
          |120,2500,25000,9223372036854775807,30.14,60.28,90.42,false,,2023-02-05,11:12:13,2023-02-05T11:12:13
          |""".stripMargin
      )
    } finally {
      RawUtils.deleteTestPath(path)
    }
  }

  test(snapi"""Csv.Read("$csvWithAllTypes", type collection(
    |    record(
    |        byteCol: byte,
    |        shortCol: short,
    |        intCol: int,
    |        longCol: long,
    |        floatCol: float,
    |        doubleCol: double,
    |        decimalCol: decimal,
    |        boolCol: bool,
    |        nullBoolCol: bool,
    |        dateCol: date,
    |        timeCol: time,
    |        timestampCol: timestamp
    |    )
    |), delimiter = ";", skip = 1)""".stripMargin) { it =>
    val path = Files.createTempFile("", "")
    try {
      it should saveToInFormat(path, "csv")
      path should contain(
        """byteCol,shortCol,intCol,longCol,floatCol,doubleCol,decimalCol,boolCol,nullBoolCol,dateCol,timeCol,timestampCol
          |1,10,100,1000,3.14,6.28,9.42,true,false,2023-12-25,01:02:03,2023-12-25T01:02:03
          |120,2500,25000,9223372036854775807,30.14,60.28,90.42,false,,2023-02-05,11:12:13,2023-02-05T11:12:13
          |""".stripMargin
      )
    } finally {
      RawUtils.deleteTestPath(path)
    }
  }

  test(snapi"""Csv.Read("$csvWithAllTypes", type collection(
    |    record(
    |        byteCol: byte,
    |        shortCol: short,
    |        intCol: int,
    |        longCol: long,
    |        floatCol: float,
    |        doubleCol: double,
    |        decimalCol: decimal,
    |        boolCol: bool,
    |        nullBoolCol: bool,
    |        dateCol: date,
    |        timeCol: time,
    |        timestampCol: timestamp
    |    )
    |), delimiter = ";", skip = 0)""".stripMargin) { it =>
    val path = Files.createTempFile("", "")
    try {
      it should saveToInFormat(path, "csv")
      if (compilerService.language.contains("rql2-truffle")) {
        path should contain(
          snapi"""byteCol,shortCol,intCol,longCol,floatCol,doubleCol,decimalCol,boolCol,nullBoolCol,dateCol,timeCol,timestampCol
            |"failed to parse CSV (location: $csvWithAllTypes: line 1, col 1), cannot parse 'byteCol' as a byte","failed to parse CSV (location: $csvWithAllTypes: line 1, col 9), cannot parse 'shortCol' as a short","failed to parse CSV (location: $csvWithAllTypes: line 1, col 18), cannot parse 'intCol' as an int","failed to parse CSV (location: $csvWithAllTypes: line 1, col 25), cannot parse 'longCol' as a long","failed to parse CSV (location: $csvWithAllTypes: line 1, col 33), cannot parse 'floatCol' as a float","failed to parse CSV (location: $csvWithAllTypes: line 1, col 42), cannot parse 'doubleCol' as a double","failed to parse CSV (location: $csvWithAllTypes: line 1, col 52), cannot parse 'decimalCol' as a decimal","failed to parse CSV (location: $csvWithAllTypes: line 1, col 63), cannot parse 'boolCol' as a bool","failed to parse CSV (location: $csvWithAllTypes: line 1, col 71), cannot parse 'nullBoolCol' as a bool","failed to parse CSV (location: $csvWithAllTypes: line 1, col 83), string 'dateCol' does not match date template 'yyyy-M-d'","failed to parse CSV (location: $csvWithAllTypes: line 1, col 91), string 'timeCol' does not match time template 'HH:mm[:ss[.SSS]]'","failed to parse CSV (location: $csvWithAllTypes: line 1, col 99), string 'timestampCol' does not match timestamp template 'HH:mm[:ss[.SSS]]'"
            |1,10,100,1000,3.14,6.28,9.42,true,false,2023-12-25,01:02:03,2023-12-25T01:02:03
            |120,2500,25000,9223372036854775807,30.14,60.28,90.42,false,,2023-02-05,11:12:13,2023-02-05T11:12:13
            |""".stripMargin
        )
      } else {
        path should contain(
          """byteCol,shortCol,intCol,longCol,floatCol,doubleCol,decimalCol,boolCol,nullBoolCol,dateCol,timeCol,timestampCol
            |"failed to parse CSV (line 1, col 1), cannot cast 'byteCol' to byte","failed to parse CSV (line 1, col 2), cannot cast 'shortCol' to short","failed to parse CSV (line 1, col 3), cannot cast 'intCol' to int","failed to parse CSV (line 1, col 4), cannot cast 'longCol' to long","failed to parse CSV (line 1, col 5), cannot cast 'floatCol' to float","failed to parse CSV (line 1, col 6), cannot cast 'doubleCol' to double","failed to parse CSV (line 1, col 7), Character d is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.","failed to parse CSV (line 1, col 8), cannot cast 'boolCol' to boolean","failed to parse CSV (line 1, col 9), cannot cast 'nullBoolCol' to boolean","failed to parse CSV (line 1, col 10), string 'dateCol' does not match date template 'yyyy-M-d'","failed to parse CSV (line 1, col 11), string 'timeCol' does not match time template 'HH:mm[:ss[.SSS]]'","failed to parse CSV (line 1, col 12), string 'timestampCol' does not match timestamp template 'yyyy-M-d['T'][ ]HH:mm[:ss[.SSS]]'"
            |1,10,100,1000,3.14,6.28,9.42,true,false,2023-12-25,01:02:03,2023-12-25T01:02:03
            |120,2500,25000,9223372036854775807,30.14,60.28,90.42,false,,2023-02-05,11:12:13,2023-02-05T11:12:13
            |""".stripMargin
        )
      }
    } finally {
      RawUtils.deleteTestPath(path)
    }
  }

  test("""[{a: 1, b: 2}, {a: 3, b: 4}]""") { it =>
    it should run
    val path = Files.createTempFile("", "")
    try {
      it should saveToInFormat(path, "csv", options = Map("windows-line-ending" -> "false"))
      path should contain("a,b\n1,2\n3,4\n")
      it should saveToInFormat(path, "csv", options = Map("windows-line-ending" -> "true"))
      path should contain("a,b\r\n1,2\r\n3,4\r\n")
    } finally {
      Files.deleteIfExists(path)
    }
  }

}
