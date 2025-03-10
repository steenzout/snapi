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

package com.rawlabs.snapi.truffle.ast.expressions.builtin.http_package;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.rawlabs.snapi.truffle.ast.ExpressionNode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@NodeInfo(shortName = "Http.UrlDecode")
@NodeChild(value = "str")
public abstract class HttpUrlDecodeNode extends ExpressionNode {
  @Specialization
  @TruffleBoundary
  public String encode(String str) {
    return URLDecoder.decode(str, StandardCharsets.UTF_8);
  }
}
