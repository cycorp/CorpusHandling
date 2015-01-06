package com.cyc.corpus.expts;

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

/**
 * Experiment is an interface for configuring the paths that the CorpusHandling project uses.
 *
 */
public interface Experiment {

  /**
   *
   * @return the base corpus directory
   */
  public  String getBaseDir();

  /**
   *
   * @return the subcorpora directory
   */
  public String getSubCorporaDir();
  
}
