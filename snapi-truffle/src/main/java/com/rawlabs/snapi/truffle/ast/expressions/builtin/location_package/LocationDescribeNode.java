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

package com.rawlabs.snapi.truffle.ast.expressions.builtin.location_package;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.rawlabs.snapi.frontend.inferrer.api.*;
import com.rawlabs.snapi.frontend.rql2.Rql2TypeUtils$;
import com.rawlabs.snapi.frontend.rql2.source.*;
import com.rawlabs.snapi.truffle.Rql2Context;
import com.rawlabs.snapi.truffle.Rql2Language;
import com.rawlabs.snapi.truffle.ast.ExpressionNode;
import com.rawlabs.snapi.truffle.runtime.list.ObjectList;
import com.rawlabs.snapi.truffle.runtime.primitives.ErrorObject;
import com.rawlabs.snapi.truffle.runtime.primitives.LocationObject;
import com.rawlabs.snapi.truffle.runtime.primitives.NullObject;
import com.rawlabs.snapi.truffle.runtime.record.RecordNodes;
import com.rawlabs.utils.core.RawException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import scala.Some;

// A.Z Similar implementation to Scala
@NodeInfo(shortName = "String.Read")
@NodeChild("location")
@NodeChild("sampleSize")
public abstract class LocationDescribeNode extends ExpressionNode {

  @Specialization
  @TruffleBoundary
  protected Object doDescribe(
      LocationObject locationObject,
      int sampleSize,
      @Cached(inline = true) RecordNodes.AddPropNode addPropNode) {
    InferrerService inferrer = Rql2Context.get(this).getInferrer();
    try {
      // In scala implementation interpreter there is a sample size argument
      InferrerOutput descriptor =
          inferrer.infer(
              AutoInferrerInput.apply(
                  locationObject.getLocation(),
                  sampleSize == Integer.MAX_VALUE ? Some.empty() : Some.apply(sampleSize)));

      String format = "";
      String comment = "";
      SourceType tipe = SourceAnyType.apply();
      Map<String, String> properties = new HashMap<>();
      boolean sampled = false;

      if (descriptor instanceof SqlTableInferrerOutput) {
        SqlTableInferrerOutput sqlTableDescriptor = (SqlTableInferrerOutput) descriptor;
        format = "relational table";
        tipe = sqlTableDescriptor.tipe();
      } else if (descriptor instanceof SqlQueryInferrerOutput) {
        SqlQueryInferrerOutput sqlQueryDescriptor = (SqlQueryInferrerOutput) descriptor;
        format = "relational query";
        tipe = sqlQueryDescriptor.tipe();
      } else if (descriptor instanceof TextInputStreamInferrerOutput) {
        TextInputStreamInferrerOutput textInputQueryDescriptor =
            (TextInputStreamInferrerOutput) descriptor;
        comment =
            String.format(
                    "encoding %s (confidence: %s",
                    textInputQueryDescriptor.encoding(), textInputQueryDescriptor.confidence())
                + "%)";
        if (textInputQueryDescriptor.format() instanceof CsvFormatDescriptor) {
          CsvFormatDescriptor csvDescriptor =
              (CsvFormatDescriptor) textInputQueryDescriptor.format();
          properties.put("has_header", String.valueOf(csvDescriptor.hasHeader()));
          properties.put("delimiter", String.valueOf(csvDescriptor.delimiter()));
          ArrayList<String> nls = new ArrayList<>();
          csvDescriptor.nulls().foreach(nls::add);
          properties.put(
              "nulls",
              nls.stream().map(x -> '"' + x + '"').collect(Collectors.joining(",", "[", "]")));

          ArrayList<String> nans = new ArrayList<>();
          csvDescriptor.nans().foreach(nans::add);
          properties.put(
              "nans",
              nans.stream().map(x -> '"' + x + '"').collect(Collectors.joining(",", "[", "]")));
          properties.put("multiLine_fields", String.valueOf(csvDescriptor.multiLineFields()));
          properties.put("skip", String.valueOf(csvDescriptor.skip()));
          if (csvDescriptor.escapeChar().isDefined()) {
            properties.put("escape", csvDescriptor.escapeChar().get().toString());
          }
          if (csvDescriptor.quoteChar().isDefined()) {
            properties.put("quote", csvDescriptor.quoteChar().get().toString());
          }
          format = "csv";
          tipe = csvDescriptor.tipe();
          sampled = csvDescriptor.sampled();
        } else if (textInputQueryDescriptor.format() instanceof JsonFormatDescriptor) {
          JsonFormatDescriptor jsonDescriptor =
              (JsonFormatDescriptor) textInputQueryDescriptor.format();
          format = "json";
          tipe = jsonDescriptor.tipe();
          sampled = jsonDescriptor.sampled();
        } else if (textInputQueryDescriptor.format() instanceof HjsonFormatDescriptor) {
          HjsonFormatDescriptor hjsonDescriptor =
              (HjsonFormatDescriptor) textInputQueryDescriptor.format();
          format = "hjson";
          tipe = hjsonDescriptor.tipe();
          sampled = hjsonDescriptor.sampled();
        } else if (textInputQueryDescriptor.format() instanceof XmlFormatDescriptor) {
          XmlFormatDescriptor xmlDescriptor =
              (XmlFormatDescriptor) textInputQueryDescriptor.format();
          format = "xml";
          tipe = xmlDescriptor.tipe();
          sampled = xmlDescriptor.sampled();
        } else if (textInputQueryDescriptor.format() instanceof LinesFormatDescriptor) {
          LinesFormatDescriptor linesDescriptor =
              (LinesFormatDescriptor) textInputQueryDescriptor.format();
          format = "lines";
          tipe = linesDescriptor.tipe();
          sampled = linesDescriptor.sampled();
          if (linesDescriptor.regex().isDefined()) {
            properties.put("regex", linesDescriptor.regex().get());
          }
        }
      }

      Rql2Type rql2Type =
          (Rql2Type) Rql2TypeUtils$.MODULE$.inferTypeToRql2Type(tipe, sampled, sampled);
      Rql2Type flatten = rql2Type;
      boolean isCollection = false;

      if (rql2Type instanceof Rql2IterableType) {
        Rql2IterableType rql2IterableType = (Rql2IterableType) rql2Type;
        flatten = (Rql2Type) rql2IterableType.innerType();
        isCollection = true;
      } else if (rql2Type instanceof Rql2ListType) {
        Rql2ListType rql2IterableType = (Rql2ListType) rql2Type;
        flatten = (Rql2Type) rql2IterableType.innerType();
        isCollection = true;
      }

      String formattedType = SourcePrettyPrinter$.MODULE$.format(rql2Type);

      Object record = Rql2Language.get(this).createPureRecord();

      addPropNode.execute(this, record, "format", format, false);
      addPropNode.execute(this, record, "comment", comment, false);
      addPropNode.execute(this, record, "type", formattedType, false);

      Object[] propRecords = new Object[properties.size()];
      // properties
      List<String> keyList = new ArrayList<>(properties.keySet());
      for (int i = 0; i < keyList.size(); i++) {
        Object rec = Rql2Language.get(this).createPureRecord();
        addPropNode.execute(this, rec, "name", keyList.get(i), false);
        if (properties.containsKey(keyList.get(i))) {
          addPropNode.execute(this, rec, "value", properties.get(keyList.get(i)), false);
        } else {
          addPropNode.execute(this, rec, "value", NullObject.INSTANCE, false);
        }
        propRecords[i] = rec;
      }
      ObjectList propList = new ObjectList(propRecords);

      addPropNode.execute(this, record, "properties", propList, false);
      addPropNode.execute(this, record, "is_collection", isCollection, false);

      // columns
      if (flatten instanceof Rql2RecordType) {
        Rql2RecordType rql2RecordType = (Rql2RecordType) flatten;
        Object[] columnRecords = new Object[rql2RecordType.atts().length()];
        for (int i = 0; i < rql2RecordType.atts().length(); i++) {
          String typeStr;
          boolean isNullable;
          Rql2TypeWithProperties fieldType =
              (Rql2TypeWithProperties) rql2RecordType.atts().apply(i).tipe();
          typeStr = SourcePrettyPrinter$.MODULE$.format(fieldType);
          isNullable = fieldType.props().contains(Rql2IsNullableTypeProperty.apply());
          Object column = Rql2Language.get(this).createPureRecord();

          addPropNode.execute(
              this, column, "col_name", rql2RecordType.atts().apply(i).idn(), false);
          addPropNode.execute(this, column, "col_type", typeStr, false);
          addPropNode.execute(this, column, "nullable", isNullable, false);
          columnRecords[i] = column;
        }
        ObjectList columnList = new ObjectList(columnRecords);
        addPropNode.execute(this, record, "columns", columnList, false);
      } else {
        String typeStr;
        boolean isNullable = false;
        if (flatten instanceof Rql2TypeWithProperties) {
          typeStr = SourcePrettyPrinter$.MODULE$.format(flatten);
          isNullable =
              ((Rql2TypeWithProperties) flatten)
                  .props()
                  .contains(Rql2IsNullableTypeProperty.apply());
        } else {
          typeStr = SourcePrettyPrinter$.MODULE$.format(flatten);
        }
        Object column = Rql2Language.get(this).createPureRecord();
        addPropNode.execute(this, column, "col_name", NullObject.INSTANCE, false);
        addPropNode.execute(this, column, "col_type", typeStr, false);
        addPropNode.execute(this, column, "nullable", isNullable, false);
        ObjectList columnList = new ObjectList(new Object[] {column});
        addPropNode.execute(this, record, "columns", columnList, false);
      }
      addPropNode.execute(this, record, "sampled", sampled, false);
      return record;
    } catch (RawException ex) {
      return new ErrorObject(ex.getMessage());
    } finally {
      inferrer.stop();
    }
  }
}
