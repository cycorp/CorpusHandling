package com.cyc.corpus.nlmpaper;

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

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;


public class PubMedOpenAccessPaperIT {


  PubMedOpenAccessPaper paper;
  PubMedOpenAccessPaper paper2;
  PubMedOpenAccessPaper paper3;

  public PubMedOpenAccessPaperIT() {
    paper = new PubMedOpenAccessPaper("PMC3499821");
    paper2 = new PubMedOpenAccessPaper("PMC3509674");
    paper3 = new PubMedOpenAccessPaper("PMC1334228");
  }


  @Test
  public void testListArchiveFiles() {

    List<Path> result = paper.listArchiveFiles();

    //The top level should have one directory
    assertEquals(11, result.size());
  }


  @Test
  public void testGetTitle() {

    String res = paper.getTitle();
    String expRes = "Filovirus Research in Gabon and Equatorial Africa: The Experience of a Research Center in the Heart of Africa";

    assertEquals(expRes, res);
  }

  @Test
  public void testGetTitle2() {

    String res = paper2.getTitle();
    String expRes = "Discovery and Early Development of AVI-7537 and AVI-7288 for the Treatment of Ebola Virus and Marburg Virus Infections";

    assertEquals(expRes, res);
  }

  @Test
  public void testGetAuthors() {
    Set<String> res = paper3.getAuthors();
    int expectedAuthorNum = 2;
    int retrievedAuthorNum = res.size();
    assertEquals(expectedAuthorNum, retrievedAuthorNum);
  }

  @Test
  public void testGetPublicationDate() {
    Date res = paper2.getPublicationDate();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String expRes = "2012-11-01";
    assertEquals(expRes, formatter.format(res));
  }

  @Test
  public void testGetJournalTitle() {
    String res = paper3.getJournalTitle();
    String expRes = "Virol J";
    assertEquals(expRes, res);
  }

  @Test
  public void testGetPublisher() {
    String res = paper3.getPublisher();
    String expRes = "BioMed Central";
    assertEquals(expRes, res);
  }

  @Test
  public void testGetPubInfo() {
    String res = paper3.getPubInfo().toString();
    String expRes = "{volume=2, fpage=92, lpage=92}";
    assertEquals(expRes, res);
  }

  @Test
  public void testGetAbstract() {

    String res = paper.getAbstract().substring(0, 40)
            + "...";
    String expRes = "Health research programs targeting the p...";
    assertEquals(expRes, res);
  }
}
