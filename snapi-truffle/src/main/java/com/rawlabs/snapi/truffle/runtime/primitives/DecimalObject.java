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

package com.rawlabs.snapi.truffle.runtime.primitives;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.rawlabs.snapi.truffle.Rql2Language;
import java.math.BigDecimal;

@ExportLibrary(InteropLibrary.class)
public final class DecimalObject implements TruffleObject {
  private final BigDecimal bigDecimal;

  public DecimalObject(BigDecimal bigDecimal) {
    this.bigDecimal = bigDecimal;
  }

  public BigDecimal getBigDecimal() {
    return bigDecimal;
  }

  @ExportMessage
  final boolean hasLanguage() {
    return true;
  }

  @ExportMessage
  final Class<? extends TruffleLanguage<?>> getLanguage() {
    return Rql2Language.class;
  }

  @ExportMessage
  final Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
    return "Decimal";
  }

  @ExportMessage
  final boolean isString() {
    return true;
  }

  @ExportMessage
  @TruffleBoundary
  final String asString() {
    return bigDecimal.toString();
  }
}
