package com.cyc.corpus.papers;

/*
 * #%L
 * CorpusHandling
 * %%
 * Copyright (C) 2014 - 2015 Cycorp, Inc
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

import com.cyc.corpus.nlmpaper.DefaultOpenAccessPaper;
import com.cyc.corpus.nlmpaper.OpenAccessPaper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * OpenAccessSubcollection is an abstract class for representing subcollections of open access
 * papers.
 *
 */
public abstract class OpenAccessSubcollection extends OpenAccessCollection {

  /**
   *
   * @param collectionName the name of the subcollection
   */
  public OpenAccessSubcollection(String collectionName) {
    super(collectionName);
  }

  /**
   *
   * @return the path to the subcorpus directory
   */
  protected abstract String subCorpusPath();

  /**
   *
   * @return the path to the used article list file
   */
  protected abstract String usedArticleListPath();
  /**
   * Load a sub-collection based on a master resource and what sub-collections
   * have already been created as stored in subCorporaDir.
   *
   * @return a collection of papers
   */
  @Override
  protected  Collection<OpenAccessPaper> load() {
    // Access to file that tracks articles already in a subcollection
    Set<OpenAccessPaper> paperSet = new HashSet<>();
    File subCorpusFile = new File(subCorpusPath());
    if (subCorpusFile.exists() && subCorpusFile.canRead()) {
      BufferedReader paperListSub;
      try {
        paperListSub = new BufferedReader(new FileReader(subCorpusFile));
        paperSet = paperListSub.lines().map(String::trim).map(DefaultOpenAccessPaper::new).collect(Collectors.toSet());
      } catch (FileNotFoundException ex) {
        Logger.getLogger(PubMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return paperSet;
  }

  /**
   *
   * @param setSize the number of papers in the collection
   * @return a collection of papers
   */
  abstract protected  Collection<OpenAccessPaper> create(int setSize);
}
