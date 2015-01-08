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

//// Internal Imports
//// External Imports
import com.cyc.corpus.nlmpaper.AIMedOpenAccessPaper;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <P>
 * AIMedCollection is designed to...
 *
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>
 * Use is subject to license terms.
 *
 * Created on : Nov 17, 2014, 4:08:23 PM
 */
public class AIMedCollection {

  private final String name;
  static final String resourceName = "AIMed-corpus-list.txt";
  //static final String resourceName = "AIMed-corpus-list-TEST.txt";
  static AIMedCollection singleton = new AIMedCollection();
  private Collection<AIMedOpenAccessPaper> myPapers;

  public static final String FS = File.separator;

  //// Constructors
  /**
   * Creates a new instance of AIMedCollection.
   * @param collectionName the name of the AIMed corpus
   */
  public AIMedCollection(String collectionName) {
    this.name = collectionName;
  }
  
  public AIMedCollection() {
    name = "AIMed Corpus Collection";
    singleton = new AIMedCollection(name);
    myPapers = this.load();
  }

  
  
  //// Public Area
  public Collection<AIMedOpenAccessPaper> getMyPapers() {
    assert myPapers != null : "they must have been loaded by here!";
    return myPapers;
  }
  
  /**
   *
   * @return an InitialCollection object
   */
  public static AIMedCollection get() {
    return singleton;
  }

  //// Protected Area

   protected Collection<AIMedOpenAccessPaper> load() {
    InputStream paperListStream;
    paperListStream = //      Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            this.getClass().getResourceAsStream(resourceName);
    assert paperListStream != null : "Can't find " + resourceName;
    BufferedReader paperList = new BufferedReader(new InputStreamReader(paperListStream));

    Predicate<String> isPaperID = (line) -> {
      return line != null && line.length() > 0 && line.startsWith("abstract");
    };
    Set<AIMedOpenAccessPaper> paperSet
            = paperList.lines()
            .map(String::trim)
            .filter(isPaperID)
            .map(AIMedOpenAccessPaper::new)
            .collect(Collectors.toSet());

    return paperSet;
  }
  
    /**
   *
   * @return the name of the collection
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return a collection of papers
   */
  public Collection<AIMedOpenAccessPaper> getPapers() {
    return myPapers;
  }

  /**
   *
   * @param papers a collection of papers
   */
  protected void setPapers(Collection<AIMedOpenAccessPaper> papers) {
    this.myPapers = papers;
  }
  //// Private Area
  //// Internal Rep
}
