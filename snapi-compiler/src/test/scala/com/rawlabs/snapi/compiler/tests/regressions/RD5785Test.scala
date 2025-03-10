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

package com.rawlabs.snapi.compiler.tests.regressions

import com.rawlabs.snapi.compiler.tests.Rql2TestContext

class RD5785Test extends Rql2TestContext {

  // The only way of this returning true is if all values in the list are the same.
  // Almost impossible with 5 random values.
  test("""let
    |    rec recurse(l: list(double), pos: int, acc: bool): bool =
    |       let
    |           stop = pos == 1,
    |           compare = acc and List.Get(l, pos) == List.Get(l, pos -1)
    |       in
    |           if (stop) then compare else recurse(l, pos-1, compare),
    |    l = [Math.Random(), Math.Random(), Math.Random(), Math.Random(), Math.Random()]
    |in
    |    recurse(l ,4, true)
    |""".stripMargin) {
    _ should evaluateTo("false")
  }

}
