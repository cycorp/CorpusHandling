package com.cyc.corpus.expts;

/*
 * #%L
 * CorpusHandling
 * %%
 * Copyright (C) 2014 Cycorp, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CorpusHandlingStrawmanExpt defines where the CorpusHandling project will place the corpora it
 * creates. It reads config.json as saved in /src/main/resources to get the base directory path and
 * the subcorpora directory path.
 *
 */
public class CorpusHandlingStrawmanExpt implements Experiment {

  String baseDir, subCorporaDir;

  public CorpusHandlingStrawmanExpt() {
    if (getClass().getClassLoader().getResourceAsStream("config.json") == null) {
      System.out.println("ERROR: It is likely that you have not created a config.json file or that it is misconfigured. Please see README for more information.");
      throw new RuntimeException();
    } else {
      try {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("config.json");
        String configContent = IOUtils.toString(stream, "UTF-8");
        JSONObject config = new JSONObject(configContent);
        this.baseDir = config.getString("baseDir");
        this.subCorporaDir = config.getString("subCorporaDir");
      } catch (JSONException | IOException e) {
        Logger.getLogger(CorpusHandlingStrawmanExpt.class.getName()).log(Level.SEVERE, null, e);
      } 
    }
  }

  @Override
  public String getBaseDir() {
    return baseDir;
  }

  @Override
  public String getSubCorporaDir() {
    return subCorporaDir;
  }
}
