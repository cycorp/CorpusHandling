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

import com.cyc.corpus.expts.CorpusHandlingStrawmanExpt;
import com.cyc.corpus.expts.Experiment;
import com.cyc.corpus.nlmpaper.OpenAccessPaper;
import com.cyc.corpus.nlmpaper.PubMedOpenAccessPaper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An open access document collection that is a subcollection of papers found in
 PubMedInitialCollection.
 *
 */
public class PubMedSubcollection extends OpenAccessSubcollection {

  static final String usedArticleListFileName = "SamplePMCCorpus_used_articles.txt";
  final static Experiment expt = new CorpusHandlingStrawmanExpt();
  final static String subCorporaDir = expt.getSubCorporaDir();

  /**
   * Creates a new instance of SubCollection. If it exists, it
   * reconstructs it. Otherwise it generates it.
   * @param subCollectionName the named subcollection to retrieve or create
   * @param setSize the number of articles that should be expected in the set
   */
  public PubMedSubcollection(String subCollectionName, int setSize) {
    super(subCollectionName);
    Collection<OpenAccessPaper> thePapers = load();
    assert thePapers.isEmpty() || thePapers.size()==setSize 
        : "loaded "+getName()+" from "+subCorpusPath()+" but it didn't have expected "+setSize+" papers";
    if (thePapers.isEmpty()) {
      thePapers = create(setSize);
    }
    setPapers(thePapers);
  }

  /**
   *
   * @param setSize the number of papers that should be in the subcollection
   * @return a collection of papers
   */
  @Override
  protected final Collection<OpenAccessPaper> create(int setSize) {
    Set<OpenAccessPaper> paperSet = new HashSet<>();
    // Access to file that tracks articles already in a subcollection
    try {
      BufferedReader paperListSub = new BufferedReader(new FileReader(new File(usedArticleListPath())));
      final Set<OpenAccessPaper> used = paperListSub.lines().map(String::trim).map(PubMedOpenAccessPaper::new).collect(Collectors.toSet());
      // This predicate checks that the article name hasn't already been used in a subCollection
      Predicate<OpenAccessPaper> isNotAlreadyUsed = (OpenAccessPaper paper) -> !used.contains(paper);
      // Access to the complete list of articles
      PubMedInitialCollection bc = PubMedInitialCollection.get();
       
      //THIS ACUALLY DOES THE CREATION
      List<OpenAccessPaper>paperList=new ArrayList<>(bc.getMyPapers());
      Collections.shuffle(paperList);
      paperSet=paperList.stream()
              .filter(isNotAlreadyUsed)
              .limit(setSize)
              .collect(Collectors
                      .toSet());
     
      //Write out the new subcorpus
       try (final PrintWriter pw = new PrintWriter(subCorpusPath())) {
        paperSet.forEach((OpenAccessPaper o) -> pw.write(o.getPaperID() + "\n"));
      } catch (FileNotFoundException ex) {
        Logger.getLogger(PubMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
      }
       
     //write out the new, augmented list of papers
      used.addAll(paperSet);
      try (final PrintWriter pw = new PrintWriter(usedArticleListPath())) {
        used.forEach((OpenAccessPaper o) -> pw.write(o.getPaperID() + "\n"));
      } catch (FileNotFoundException ex) {
        Logger.getLogger(PubMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
      }
      
    } catch (FileNotFoundException ex) {
      Logger.getLogger(PubMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
    }
   return paperSet; 
  }

  /**
   *
   * @return a string representing the path the used articles list
   */
  @Override
  protected String subCorpusPath() {
    return subCorporaDir + FS + getName() + "_used_articles.txt";
  }

  /**
   *
   * @return a string representing the path to the used article file as specified above
   */
  @Override
  protected String usedArticleListPath() {
    return subCorporaDir + FS + usedArticleListFileName;
  }
}
