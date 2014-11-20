package com.cyc.corpus.papers;

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

import com.cyc.corpus.nlmpaper.OpenAccessPaper;
import com.cyc.corpus.nlmpaper.PubMedOpenAccessPaper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An open access document collection that is loaded from a resource.
 *
 */
public class PubMedInitialCollection extends OpenAccessCollection {

  static final String resourceName = "SamplePMCCorpus.txt";
  static final PubMedInitialCollection singleton = new PubMedInitialCollection();
  final Collection<OpenAccessPaper> myPapers;

  /**
   *
   * @return a collection of papers
   */
  public Collection<OpenAccessPaper> getMyPapers() {
   assert myPapers!= null : "they must have been loaded by here!";
    return myPapers;
  }
  
  PubMedInitialCollection() {
    super("Sample PMC Corpus Collection");
    myPapers=this.load();
  }

  /**
   *
   * @return an PubMedInitialCollection object
   */
  public static PubMedInitialCollection get() {
    return singleton;
  }  
  
  /**
   * Load a collection based on this class's knowledge of what resource to get 
 the list from
   * @return the collection of papers
   */
  @Override
  final protected Collection<OpenAccessPaper> load() {
    InputStream paperListStream;    
      paperListStream = //      Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            this.getClass().getResourceAsStream(resourceName);
    assert paperListStream != null : "Can't find "+resourceName;
    BufferedReader paperList = new BufferedReader(new InputStreamReader(paperListStream));
   
    Predicate<String> isPaperID = (line) -> {
      return line != null && line.length() > 0 && line.startsWith("PMC");
    };
    Set<OpenAccessPaper> paperSet
        = paperList.lines()
        .map(String::trim)
        .filter(isPaperID)
        .map(PubMedOpenAccessPaper::new)
        .collect(Collectors.toSet());
    
    return paperSet;
  }
}
