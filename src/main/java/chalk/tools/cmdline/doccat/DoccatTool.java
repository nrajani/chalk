/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package chalk.tools.cmdline.doccat;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import chalk.tools.cmdline.AbstractBasicCmdLineTool;
import chalk.tools.cmdline.CLI;
import chalk.tools.cmdline.CmdLineUtil;
import chalk.tools.cmdline.PerformanceMonitor;
import chalk.tools.doccat.DoccatModel;
import chalk.tools.doccat.DocumentCategorizerME;
import chalk.tools.doccat.DocumentSample;
import chalk.tools.util.ObjectStream;
import chalk.tools.util.ParagraphStream;
import chalk.tools.util.PlainTextByLineStream;


public class DoccatTool extends AbstractBasicCmdLineTool {

  public String getShortDescription() {
    return "learnable document categorizer";
  }
  
  public String getHelp() {
    return "Usage: " + CLI.CMD + " " + getName() + " model < documents";
  }

  public void run(String[] args) {
    
    if (0 == args.length) {
      System.out.println(getHelp());
    } else {

      DoccatModel model = new DoccatModelLoader().load(new File(args[0]));

      DocumentCategorizerME doccat = new DocumentCategorizerME(model);

      ObjectStream<String> documentStream = new ParagraphStream(
          new PlainTextByLineStream(new InputStreamReader(System.in)));

      PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "doc");
      perfMon.start();

      try {
        String document;
        while ((document = documentStream.read()) != null) {
          double prob[] = doccat.categorize(document);
          String category = doccat.getBestCategory(prob);

          DocumentSample sample = new DocumentSample(category, document);
          System.out.println(sample.toString());

          perfMon.incrementCounter();
        }
      }
      catch (IOException e) {
        CmdLineUtil.handleStdinIoError(e);
      }

      perfMon.stopAndPrintFinalResult();
    }
  }
}
