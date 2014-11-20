/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.corpus.nlmpaper;

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

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author arebguns
 */
public class AIMedOpenAccessPaperIT {

  AIMedOpenAccessPaper paper;
  AIMedOpenAccessPaper paper2;
  AIMedOpenAccessPaper paper3;

  public AIMedOpenAccessPaperIT() {
    paper  = new AIMedOpenAccessPaper("abstract9-23");
    paper2 = new AIMedOpenAccessPaper("abstract1-100");
    paper3 = new AIMedOpenAccessPaper("abstract1-110");
  }

  @Test
  public void testListArchiveFiles() {
    List<Path> result = paper.listArchiveFiles();

    // we are expecting 750 files in the directory
    assertEquals(750, result.size());
  }

  @Test
  public void testGetTitle() {
    String res = paper.getTitle();
    String expRes = "HIV-1 drug resistance profiles in children and adults with viral load of <50 copies/ml receiving combination therapy.";

    assertEquals(expRes, res);
  }

  @Test
  public void testGetTitle2() {
    String res = paper2.getTitle();
    String expRes = "Induction of urokinase-type plasminogen activator by the anthracycline antibiotic in human RC-K8 lymphoma and H69 lung-carcinoma cells.";

    assertEquals(expRes, res);
  }

  @Test
  public void testGetTitle3() {
    String res = paper3.getTitle();
    String expRes = "Fluorescent 5'-exonuclease assay for the absolute quantification of Wilms' tumour gene (WT1) mRNA: implications for monitoring human leukaemias.";

    assertEquals(expRes, res);
  }

  @Test
  public void testTaggedProteins() {
    Set<String> res = paper.getTaggedProteins();
    assertEquals(0, res.size());
  }

  @Test
  public void testTaggedProteins2() {
    Set<String> res = paper2.getTaggedProteins();
    
    // Expecting [plasminogen, plasmin, urokinase, uPA, plasminogen activator, IL-1beta]
    assertTrue(res.contains("plasminogen"));
    assertTrue(res.contains("plasmin"));
    assertTrue(res.contains("urokinase"));
    assertTrue(res.contains("uPA"));
    assertTrue(res.contains("plasminogen activator"));
    assertTrue(res.contains("IL-1beta"));
    
    assertEquals(6, res.size());
  }

  @Test
  public void testTaggedProteins3() {
    Set<String> res = paper3.getTaggedProteins();

    // Expecting [bcr, WT1, abl]
    assertTrue(res.contains("bcr"));
    assertTrue(res.contains("WT1"));
    assertTrue(res.contains("abl"));
    
    assertEquals(3, res.size());
  }
}
