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
import java.io.File;
import java.util.Collection;

/**
 * An abstract class representing a collection of open access papers.
 *
 */
public abstract class OpenAccessCollection {

  private final String name;
  private Collection<OpenAccessPaper> papers;

  /**
   *
   */
  public static final String FS = File.separator;

  /**
   *
   * @param collectionName the name of the collection
   */
  public OpenAccessCollection(String collectionName) {
    this.name = collectionName;
  }

  /**
   *
   * @return a collection of papers
   */
  abstract protected Collection<OpenAccessPaper> load();

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
  public Collection<OpenAccessPaper> getPapers() {
    return papers;
  }

  /**
   *
   * @param papers a collection of papers
   */
  protected void setPapers(Collection<OpenAccessPaper> papers) {
    this.papers = papers;
  }
}
