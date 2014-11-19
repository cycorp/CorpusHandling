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

import org.junit.Test;
import static org.junit.Assert.*;

public class SubcollectionIT {

  public SubcollectionIT() {
  }

  /**
   * Test of get method, of class Subcollection.
   */
  @Test
  public void testSubcollectionRetrieval() {
    System.out.println("get");
    Subcollection result = new Subcollection("sampleCorpus1", 20);
    assertEquals(20, result.getPapers().size());
  }

  @Test
  public void testSubcollectionCreation() {
    System.out.println("create");
    Subcollection result = new Subcollection("sampleCorpus2", 20);
    assertEquals(20, result.getPapers().size());
  }

}
