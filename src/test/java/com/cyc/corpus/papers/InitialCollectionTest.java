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

import com.cyc.corpus.papers.InitialCollection;
import com.cyc.corpus.nlmpaper.PubMedOpenAccessPaper;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;

public class InitialCollectionTest {

  public InitialCollectionTest() {
  }

  /**
   * Test of get method, of class InitialCollection.
   */
  @Test
  public void testGet() {
    System.out.println("get");
    InitialCollection result = InitialCollection.get();
    assertNotNull(result);
  }

  /**
   * Test of load method, of class InitialCollection. 
   */
  @Test
  public void testLoad() {
    System.out.println("load");
    InitialCollection instance = InitialCollection.get();

    Collection<PubMedOpenAccessPaper> resultC = instance.getMyPapers();
    int result = resultC.size();
    int expResult = 40;
    assertEquals(expResult, result);

  }

}
