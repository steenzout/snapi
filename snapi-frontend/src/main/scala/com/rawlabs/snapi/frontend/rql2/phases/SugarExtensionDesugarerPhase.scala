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

package com.rawlabs.snapi.frontend.rql2.phases

import com.rawlabs.snapi.frontend.base.Phase
import com.rawlabs.snapi.frontend.base.errors.ErrorCompilerMessage
import com.rawlabs.snapi.frontend.base.source.Type
import com.rawlabs.snapi.frontend.rql2.source._
import com.rawlabs.snapi.frontend.rql2.extensions.SugarEntryExtension
import com.rawlabs.snapi.frontend.rql2.{FunAppPackageEntryArguments, PipelinedPhase, Tree}
import org.bitbucket.inkytonik.kiama.rewriting.Rewriter._
import org.bitbucket.inkytonik.kiama.rewriting.Strategy

class SugarExtensionDesugarerPhase(protected val parent: Phase[SourceProgram], protected val phaseName: String)(
    protected val baseProgramContext: com.rawlabs.snapi.frontend.base.ProgramContext
) extends PipelinedPhase {

  override protected def execute(program: SourceProgram): SourceProgram = {
    desugar(program)
  }

  private def getSugarEntryExtension(p: Type): Option[SugarEntryExtension] = {
    p match {
      case PackageEntryType(pkgName, entName) => programContext.getPackage(pkgName) match {
          case Some(pkg) => pkg.getEntry(entName) match {
              case s: SugarEntryExtension => Some(s)
              case _ => None
            }
          case _ => None
        }
      case _ => None
    }
  }

  private def desugar(program: SourceProgram): SourceProgram = {
    val tree = new Tree(program)
    lazy val analyzer = tree.analyzer

    lazy val s: Strategy = attempt(sometd(rulefs[Any] {
      case f @ FunApp(p @ Proj(e, i), _) if getSugarEntryExtension(analyzer.tipe(p)).isDefined =>
        congruence(s, s) <* rule[Any] {
          case FunApp(np, nargs) =>
            val PackageType(pkgName) = analyzer.tipe(e)
            val ent = getSugarEntryExtension(analyzer.tipe(p)).get
            val FunAppPackageEntryArguments(mandatoryArgs, optionalArgs, varArgs, _) =
              analyzer.getArgumentsForFunAppPackageEntry(f, ent) match {
                case Right(x) => x.get
                case Left(error: ErrorCompilerMessage) =>
                  // The code call to this package extension isn't typing (wrong parameters?)
                  // Report the error. That can happen when developing of a sugar package extension.
                  // Wrong desugaring isn't caught otherwise.
                  throw new AssertionError(s"SugarExtensionDesugarer failed while desugaring $pkgName.$i: $error")
              }

            ent.desugar(analyzer.tipe(f), nargs, mandatoryArgs, optionalArgs, varArgs)
        }
    }))

    val r = rewrite(s)(tree.root)
    logger.trace("SugarExtensionDesugarer:\n" + format(r))
    r
  }

}
