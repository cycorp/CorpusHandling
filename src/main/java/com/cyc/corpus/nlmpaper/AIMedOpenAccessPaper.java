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

import com.cyc.corpus.expts.CorpusHandlingStrawmanExpt;
import com.cyc.corpus.expts.Experiment;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.truevfs.access.TPath;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author arebguns
 */
public class AIMedOpenAccessPaper implements OpenAccessPaper, Serializable {
  
  private static final Logger LOG = Logger.getLogger(AIMedOpenAccessPaper.class.getName());
  private static final Experiment expt = new CorpusHandlingStrawmanExpt();
  private static final String basedir = expt.getBaseDir();
  private static final String BASE_URL = "ftp://ftp.cs.utexas.edu/pub/mooney/bio-data/";
  private static final String PROTEINS_URL = BASE_URL + "proteins.tar.gz";
  private static final String INTERACTIONS_URL = BASE_URL + "interactions.tar.gz";
  
  private final String paperID;
  private AIMedArticle paper = null;
  
  public AIMedOpenAccessPaper(String paperID) {
    this.paperID = paperID;
  }

  @Override
  public String getPaperID() {
    return paperID;
  }

  /**
   *
   * @return a list of paths to the archived files
   */
  public List<Path> listArchiveFiles() {
    getZipArchive();
    Path archiveDir = new TPath(cachedArchivePath());
    return listArchiveFiles(archiveDir);
  }

  private List<Path> listArchiveFiles(Path underHere) {
    List res = new ArrayList<>();
    
    try {
      Files.newDirectoryStream(underHere).forEach((p) -> {
        if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
          res.add(p);
        } else if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
          res.addAll(listArchiveFiles(p));
        }
      });
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    
    return res;
  }
  
  protected Optional<Path> getPaperPath() {
    return listArchiveFiles()
            .stream()
            .filter((p) -> p.toString().toLowerCase().endsWith(paperID.toLowerCase()))
            .findAny();
  }

  private Optional<AIMedArticle> get() {
    if (paper != null) {
      return Optional.of(paper);
    }

    try {
      Optional<Path> xmlpath = getPaperPath();
      if (xmlpath.isPresent()) {
        Path destinationD = Paths.get(basedir, "AIMedOpenAccess", "XML");
        
        if (Files.notExists(destinationD, LinkOption.NOFOLLOW_LINKS)) {
          Files.createDirectory(destinationD);
        }
        
        Path extractTo = Paths.get(basedir, "AIMedOpenAccess", "XML", xmlpath.get().getFileName().toString());
        
        Files.copy(xmlpath.get(), extractTo, StandardCopyOption.REPLACE_EXISTING);
        
        // specify the location and name of xml file to be read  
        File xmlFile = extractTo.toFile();
        String articleString = FileUtils.readFileToString(xmlFile).trim();
        paper = new AIMedArticle(articleString);
        
        return Optional.of(paper);
      }
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, ex.getMessage(), ex);
      return Optional.empty();
    }

    return Optional.empty();
  }
  
  /**
   *
   * @return true if the article is found
   */
  public boolean isPresent() {
    return get().isPresent();
  }

  private AIMedArticle article() {
    assert isPresent() : "article should only be called for present paper";
    return get().get();
  }

  /**
   *
   * @return the title of the article
   */
  public String getTitle() {
    if (isPresent()) {
      return article().getTitle();
    }
    
    throw new RuntimeException("Attempt to get title for non-present article " + paperID);
  }
  
  @Override
  public String getAbstract() {
    if (isPresent()) {
      return article().getAbstractText();
    }
    
    throw new RuntimeException("Attempt to get abstract for non-present article " + paperID);
  }
  
  public Set<String> getTaggedProteins() {
    if (isPresent()) {
      return article().getTaggedProteins();
    }

    throw new RuntimeException("Attempt to get tagged protein set for non-present article " + paperID);
  }

  private Optional<Path> getZipArchive() {
    return getZipArchive(true);
  }

  private Path cachedArchivePath() {
    return Paths.get(basedir, "AIMedOpenAccess", "proteins.tar.gz");
  }

  private Optional<Path> getZipArchive(boolean cached) {
    try {
      Path cachedFilePath = cachedArchivePath();
      Path parentDirectoryPath = cachedFilePath.getParent();
      
      // create the parent directory it doesn't already exist
      if (Files.notExists(parentDirectoryPath, LinkOption.NOFOLLOW_LINKS)) {
        Files.createDirectory(parentDirectoryPath);
      }

      // if cached file already exist - return it, no download necessary
      if (cached && Files.exists(cachedFilePath, LinkOption.NOFOLLOW_LINKS)) {
        return Optional.of(cachedFilePath.toAbsolutePath());
      }
      
      // otherwise, download the file
      URL url = new URL(PROTEINS_URL);
      Files.copy(url.openStream(), cachedFilePath, StandardCopyOption.REPLACE_EXISTING);
      return Optional.of(cachedFilePath.toAbsolutePath());
      
    } catch (MalformedURLException ex) {
      LOG.log(Level.SEVERE, ex.getMessage(), ex);
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, ex.getMessage(), ex);
    }
    
    return Optional.empty();
  }

  
  public static void main(String[] args) {
    AIMedOpenAccessPaper m = new AIMedOpenAccessPaper("abstract1-110");
    List<Path> listArchiveFiles = m.listArchiveFiles();
    
    for (Path listArchiveFile : listArchiveFiles) {
      System.out.println(listArchiveFile);
    }
    
    System.out.println("FOUND:    " + m.getPaperPath());
    System.out.println("TITLE:    " + m.getTitle());
    System.out.println("ABSTRACT: " + m.getAbstract());
    System.out.println("PROTS:    " + m.getTaggedProteins());
  }
}

class AIMedArticle {
  
  private static final Logger LOGGER = Logger.getLogger(AIMedArticle.class.getName());
  
  private String titleSectionText;
  private String abstractSectionText;
  private Set<String> taggedProteins; 

  public AIMedArticle(String xmlFileContents) {
    taggedProteins = new HashSet<>();
    
    String[] lines = xmlFileContents.split("\n");

    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      for (String line : lines) {
        InputSource is = new InputSource(new StringReader(line));
        Document document = docBuilder.parse(is);
        NodeList nodeList = document.getElementsByTagName("*");
        
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);

          if (node.getNodeType() == Node.ELEMENT_NODE) {
            final String nodeName = node.getNodeName();
            final String textContent = node.getTextContent();

            if (nodeName.equalsIgnoreCase("ArticleTitle")) {
              titleSectionText = textContent;
            } else if (nodeName.equalsIgnoreCase("AbstractText")) {
              abstractSectionText = textContent;
            } else if (nodeName.equalsIgnoreCase("prot")) {
              taggedProteins.add(textContent);
            }
          }
        }
      }
    } catch (DOMException | IOException | ParserConfigurationException | SAXException ex) {
      Logger.getLogger(AIMedOpenAccessPaper.class.getName()).log(Level.SEVERE, null, ex);
      //throw new RuntimeException(ex);
    }
  }

  public String getTitle() {
    return titleSectionText;
  }

  public String getAbstractText() {
    return abstractSectionText;
  }

  public Set<String> getTaggedProteins() {
    return taggedProteins;
  }
}
