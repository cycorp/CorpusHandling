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
import com.cyc.corpus.expts.CorpusHandlingStrawmanExpt;
import com.cyc.corpus.expts.Experiment;
import com.cyc.corpus.nlmpaper.AIMedOpenAccessPaper;
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
 * <P>
 * AIMedSubcollection is designed to...
 *
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>
 * Use is subject to license terms.
 *
 * Created on : Nov 17, 2014, 4:12:30 PM
 */
public class AIMedSubcollection extends AIMedCollection {

  static final String usedArticleListFileName = "AIMedCorpus_used_articles.txt";
  final static Experiment expt = new CorpusHandlingStrawmanExpt();
  final static String subCorporaDir = expt.getSubCorporaDir();

  //// Constructors
  public AIMedSubcollection(String subCollectionName, int setSize) {
   super(subCollectionName);
   Collection<AIMedOpenAccessPaper> thePapers = load();
   assert thePapers.isEmpty() || thePapers.size()==setSize 
        : "loaded "+getName()+" from "+subCorpusPath()+" but it didn't have expected "+setSize+" papers";
    if (thePapers.isEmpty()) {
      thePapers = create(setSize);
    }
    setPapers(thePapers);
  }
  
  //// Public Area
  //// Protected Area
  @Override
    protected final Collection<AIMedOpenAccessPaper> load() {
    // Access to file that tracks articles already in a subcollection
    Set<AIMedOpenAccessPaper> paperSet = new HashSet<>();
    File subCorpusFile = new File(subCorpusPath());
    if (subCorpusFile.exists() && subCorpusFile.canRead()) {
      BufferedReader paperListSub;
      try {
        paperListSub = new BufferedReader(new FileReader(subCorpusFile));
        paperSet = paperListSub.lines().map(String::trim).map(AIMedOpenAccessPaper::new).collect(Collectors.toSet());
      } catch (FileNotFoundException ex) {
        Logger.getLogger(AIMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return paperSet;
  }
  
  protected final Collection<AIMedOpenAccessPaper> create(int setSize) {
   Set<AIMedOpenAccessPaper> paperSet = new HashSet<>();
   try {
      BufferedReader paperListSub = new BufferedReader(new FileReader(new File(usedArticleListPath())));
      final Set<AIMedOpenAccessPaper> used = paperListSub.lines().map(String::trim).map(AIMedOpenAccessPaper::new).collect(Collectors.toSet());
      // This predicate checks that the article name hasn't already been used in a subCollection
      Predicate<AIMedOpenAccessPaper> isNotAlreadyUsed = (AIMedOpenAccessPaper paper) -> !used.contains(paper);
      // Access to the complete list of articles
      AIMedCollection bc = AIMedCollection.get();
       
      //THIS ACUALLY DOES THE CREATION
      List<AIMedOpenAccessPaper>paperList=new ArrayList<>(bc.getMyPapers());
      Collections.shuffle(paperList);
      paperSet=paperList.stream().filter(isNotAlreadyUsed).limit(setSize).collect(Collectors.toSet());
     
      //Write out the new subcorpus
       try (final PrintWriter pw = new PrintWriter(subCorpusPath())) {
        paperSet.forEach((AIMedOpenAccessPaper o) -> pw.write(o.getPaperID() + "\n"));
      } catch (FileNotFoundException ex) {
        Logger.getLogger(AIMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
      }
       
     //write out the new, augmented list of papers
      used.addAll(paperSet);
      try (final PrintWriter pw = new PrintWriter(usedArticleListPath())) {
        used.forEach((AIMedOpenAccessPaper o) -> pw.write(o.getPaperID() + "\n"));
      } catch (FileNotFoundException ex) {
        Logger.getLogger(AIMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
      }
      
    } catch (FileNotFoundException ex) {
      Logger.getLogger(AIMedSubcollection.class.getName()).log(Level.SEVERE, null, ex);
    }
   return paperSet; 
  }
  
  
  /**
   *
   * @return a string representing the path the used articles list
   */
  protected String subCorpusPath() {
    return subCorporaDir + FS + getName() + "_used_articles.txt";
  }

  /**
   *
   * @return a string representing the path to the used article file as specified above
   */
  protected String usedArticleListPath() {
    return subCorporaDir + FS + usedArticleListFileName;
  }
  //// Private Area
  //// Internal Rep
}
