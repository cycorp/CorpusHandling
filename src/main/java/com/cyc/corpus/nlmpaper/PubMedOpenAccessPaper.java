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
import gov.nih.nlm.jats.Abstract;
import gov.nih.nlm.jats.Article;
import gov.nih.nlm.jats.ArticleTitle;
import gov.nih.nlm.jats.Contrib;
import gov.nih.nlm.jats.ContribGroup;
import gov.nih.nlm.jats.Day;
import gov.nih.nlm.jats.Fpage;
import gov.nih.nlm.jats.Issue;
import gov.nih.nlm.jats.Italic;
import gov.nih.nlm.jats.JournalTitle;
import gov.nih.nlm.jats.Lpage;
import gov.nih.nlm.jats.Month;
import gov.nih.nlm.jats.Name;
import gov.nih.nlm.jats.P;
import gov.nih.nlm.jats.PubDate;
import gov.nih.nlm.jats.Publisher;
import gov.nih.nlm.jats.PublisherName;
import gov.nih.nlm.jats.Sec;
import gov.nih.nlm.jats.Sup;
import gov.nih.nlm.jats.Title;
import gov.nih.nlm.jats.Volume;
import gov.nih.nlm.jats.Year;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import net.java.truevfs.access.TPath;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// This is a test for Git commits.
/**
 * The NLM makes a number of papers available via FTP as OpenAccess papers. This class encapsulates
 * such papers.
 *
 *
 * IMPORTANT: XSDs are required for JAXB. DTDs are required in the directory from which the get is
 * unmarshalled The files needed are at: ftp://ftp.ncbi.nlm.nih.gov/pub/jats/archiving/1.0/
 *
 */
public class PubMedOpenAccessPaper implements OpenAccessPaper, Serializable {

  final static Experiment expt = new CorpusHandlingStrawmanExpt();
  final static String basedir = expt.getBaseDir();
  private final String paperID;
  Article paper = null;
  private String titleCache = null;
  private String abstractCache = null;

  /**
   *
   * @param paperID the PMC id for the paper
   */
  public PubMedOpenAccessPaper(String paperID) {
    this.paperID = paperID;
  }

  private String toURL() {
    return "http://www.pubmedcentral.nih.gov/utils/oa/oa.fcgi?id=" + paperID;
  }

  private Optional<String> getTGZLink() {
    Predicate<String> isLink = (line) -> {
      return line.contains("format=\"tgz\"");
    };
    Function<String, String> extract = (line) -> {
      return line.replaceFirst("^.*?href=\"(.*?)\".*$", "$1");
    };

    try (BufferedReader recordReader = new BufferedReader(new InputStreamReader((new URL(toURL())).openStream()))) {
      String res
              = recordReader.lines()
              .filter(isLink)
              .map(extract)
              .collect(Collectors.joining());
      if (res.length() > 0) {
        return Optional.of(res);
      }

    } catch (IOException ex) {
      Logger.getLogger(PubMedOpenAccessPaper.class.getName()).log(Level.SEVERE, null, ex);
    }
    return Optional.empty();
  }

  private Optional<Path> getZipArchive() {
    return getZipArchive(true);
  }

  private Path cachedZIPPath() {
    return Paths.get(basedir, "NLMOpenAccess", paperID + ".tar.gz");
  }

  private Optional<Path> getZipArchive(boolean cached) {
    try {
      Path destinationD = cachedZIPPath().getParent();
      if (Files.notExists(destinationD, LinkOption.NOFOLLOW_LINKS)) {
        Files.createDirectory(destinationD);
      }

      Path destination = cachedZIPPath();
      //The file is already there, and doesn't need to be gotten
      if (cached && Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
        return Optional.of(destination.toAbsolutePath());
      }
      //Need to retrieve the file
      Optional<String> link = getTGZLink();
      if (link.isPresent()) {
        URL url = new URL(link.get());
        Files.copy(url.openStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return Optional.of(destination.toAbsolutePath());
      }
    } catch (MalformedURLException ex) {
      Logger.getLogger(PubMedOpenAccessPaper.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(PubMedOpenAccessPaper.class.getName()).log(Level.SEVERE, null, ex);
    }
    return Optional.empty();
  }

  static private String extractSiteFromFTPURL(String url) {
    Pattern p = Pattern.compile("^ftp://(.*?)/.*$");
    Matcher m = p.matcher(url);
    if (m.matches()) {
      return m.group(1);
    }
    return null;
  }

  /**
   *
   * @return a list of paths to the archived files
   */
  public List<Path> listArchiveFiles() {
    getZipArchive();
    Path archiveDir = new TPath(cachedZIPPath());
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
      Logger.getLogger(PubMedOpenAccessPaper.class.getName()).log(Level.SEVERE, null, ex);
    }
    return res;
  }

  //reduced the formatting in an NLM JATS or similar document section
  private String unfancy(List<Serializable> xmlParts) {
    String res = xmlParts.stream()
            .filter((o) -> !(o instanceof Sup)) // delete superscript sections
            .map((Object o) -> {
              if (o instanceof Italic) {  //recursively replace italic sections by their content
                Italic ital = (Italic) o;
                return unfancy(ital.getContent());
              } else if (o instanceof String) { // retain strings
                return o;

              } else {
                System.out.println("Unfancy didn't know what to do with " + o.getClass().getCanonicalName() + " " + o.toString());
                return "[DELETED]";
              }
            })
            .map((Object o) -> o.toString().trim())
            .collect(Collectors.joining(" ")).replaceAll("\\s+", " ");
    return res.trim();
  }

  /**
   *
   * @return the title of the article
   */
  public String getTitle() {
    if (isPresent()) {
      ArticleTitle title = article().getFront().getArticleMeta().getTitleGroup().getArticleTitle();
      String titleString = unfancy(title.getContent());
      return titleString;
    }
    throw new RuntimeException("Attempt to get title for non-present article " + paperID);
  }

  /**
   *
   * @return a set of Strings consisting of all of the authors listed as contributers on the paper
   */
  public Set<String> getAuthors() {
    Set<String> authors = new HashSet<>();
    if (isPresent()) {
      ContribGroup contribGroup = (ContribGroup) article().getFront().getArticleMeta().getContribGroupsAndAfvesAndAffAlternatives().get(0);
      List<Serializable> authorGroup = contribGroup.getContribsAndAddressesAndAfves();
      authorGroup.forEach(c -> {
        if (c instanceof Contrib) {
          Contrib contrib = (Contrib) c;
          if ("author".equals(contrib.getContribType()) || contrib.getContribType() == null) {
            List<Serializable> contribList = contrib.getContribIdsAndAnonymousesAndCollabs();
            contribList.forEach(n -> {
              if (n instanceof Name) {
                Name name = (Name) n;
                authors.add(name.getGivenNames().getContent().get(0) + " " + name.getSurname().getContent().get(0));
              }
            });
          }
        }
      });
    }
    return authors;
  }

  /**
   *
   * Attempts to figure out the publication date for the article. Since articles often list multiple
   * dates, we may have to just choose one to stand in as the publication date.
   * 
   * @return a date
   */
  public Date getPublicationDate() {
    Date pubDate = null;
    String pubDateStr = "";
    Map pubDateMap = new HashMap();
    if (isPresent()) {
      List<PubDate> pubDates = article().getFront().getArticleMeta().getPubDates();
      pubDates.forEach(d -> {
        if (("collection".equals(d.getPubType())) || ("pmc-release".equals(d.getPubType()))) {
          List<Serializable> dateInfo = d.getDaiesAndMonthsAndYears();
          dateInfo.forEach(i -> {
            if (i instanceof Day) {
              Day day = (Day) i;
              String dayNumber = day.getContent();
              pubDateMap.put(2, dayNumber);
            } else if (i instanceof Year) {
              Year year = (Year) i;
              String yearNumber = year.getContent();
              pubDateMap.put(0, yearNumber);
            } else if (i instanceof Month) {
              Month month = (Month) i;
              String monthNumber = month.getContent();
              pubDateMap.put(1, monthNumber);
            }
          });
        }
      });
      if (pubDateMap.isEmpty()) {
        System.out.println("Defaulting to first returned date because pubTypes are missing.");
        List<Serializable> dateInfo = pubDates.get(0).getDaiesAndMonthsAndYears(); 
        dateInfo.forEach(i -> {
          if (i instanceof Day) {
            Day day = (Day) i;
            String dayNumber = day.getContent();
            pubDateMap.put(2, dayNumber);
          } else if (i instanceof Year) {
            Year year = (Year) i;
            String yearNumber = year.getContent();
            pubDateMap.put(0, yearNumber);
          } else if (i instanceof Month) {
            Month month = (Month) i;
            String monthNumber = month.getContent();
            pubDateMap.put(1, monthNumber);
          }
        });
      }
      try {
        if (pubDateMap.containsKey(2)) {
          pubDateStr = pubDateMap.get(0).toString() + "-" + pubDateMap.get(1).toString() + "-" + pubDateMap.get(2).toString();
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
          pubDate = formatter.parse(pubDateStr);
        } else if (pubDateMap.containsKey(1)) {
          pubDateStr = pubDateMap.get(0).toString() + "-" + pubDateMap.get(1).toString();
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
          pubDate = formatter.parse(pubDateStr);
        } else {
          pubDateStr = pubDateMap.get(0).toString();
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
          pubDate = formatter.parse(pubDateStr);
        }
      } catch (ParseException e) {
        System.out.println("Unable to parse date");
      }
    }

    return pubDate;
  }

  /**
   *
   * Returns a map with some additional information that is needed to reference the article.
   * The returned map may have the following keys: "volume", "issue", "fpage", and "lpage".
   * 
   * @return a map consisting of some publication information
   */
  public Map getPubInfo() {
    Map collectionInfo = new HashMap();
    if (isPresent()) {
      try {
        Volume v = article().getFront().getArticleMeta().getVolume();
        collectionInfo.put("volume", v.getContent().get(0));
      } catch (Exception e) {
        System.out.println("No volume information found");
      }
      try {
        Issue i = article().getFront().getArticleMeta().getIssues().get(0);
        collectionInfo.put("issue", i.getContent().get(0));
      } catch (Exception e) {
        System.out.println("No issue information found");
      }
      try {
        Fpage firstPage = article().getFront().getArticleMeta().getFpage();
        collectionInfo.put("fpage", firstPage.getContent());
      } catch (Exception e) {
        System.out.println("No firstpage information found");
      }
      try {
        Lpage lastPage = article().getFront().getArticleMeta().getLpage();
        collectionInfo.put("lpage", lastPage.getContent());
      } catch (Exception e) {
        System.out.println("No lastpage information found");
      }
    }
    return collectionInfo;
  }

  /**
   *
   * @return the title of the publication in which the article was published.
   */
  public String getJournalTitle() {
    if (isPresent()) {
      try {
        List<JournalTitle> journalTitleList = article().getFront().getJournalMeta().getJournalTitleGroups().get(0).getJournalTitles();
        String journalTitleString = unfancy(journalTitleList.get(0).getContent());
        // titleCache = Optional.of(titleString);
        return journalTitleString;
      } catch (Exception e) {
        System.out.println("No journal title groups found.  Attempting to use journal IDs instead.");
        return article().getFront().getJournalMeta().getJournalIds().get(0).getContent();
      }
    }
    throw new RuntimeException("Attempt to get journal title for non-present article " + paperID);
  }

  /**
   *
   * @return the publisher of the article
   */
  public String getPublisher() {
    String publisherString = "";
    List<Object> pubList = new ArrayList<>();
    if (isPresent()) {
      try {
        Publisher publisher = article().getFront().getJournalMeta().getPublisher();
        List<Serializable> publisherInfo = publisher.getPublisherNamesAndPublisherLocs();
        publisherInfo.forEach(p -> {
          if (p instanceof PublisherName) {
            PublisherName name = (PublisherName) p;
            pubList.add(unfancy(name.getContent()));
          }
        });
        publisherString = pubList.get(0).toString();
        return publisherString;
      } catch (Exception e) {
        System.out.println("No publisher information found");
      }
      //throw new RuntimeException("Attempt to get publisher title for non-present article " + paperID);
    }
    return "unknown";
  }

  /**
   * Gets the abstract for this paper, downloading it from NLM if necessary.. Throws an exception if
   * it can't get the paper. The abstract is stripped of
   * sup sections and italic sections are normalized to plain text.
   *
   * @return String with the abstract
   */
  public String getAbstract() {
    final List<Serializable> complexParts = new ArrayList<>();
    if (isPresent()) {
      String res;
      try {
        Abstract abs = article().getFront().getArticleMeta()
                .getAbstracts().get(0);
        System.out.println("ABS" + abs);
        try {
          P paragraph = (P) abs.getAddressesAndAlternativesAndArraies().get(0);
          List<Serializable> parts = paragraph.getContent();
          res = unfancy(parts);
        } catch (Exception e) {
          System.out.println("Abstract may have complex structure...");
          java.util.List<Sec> sections = abs.getSecs();
          sections.forEach((s) -> {
            try {
              Title t = s.getTitleElement();
              System.out.println("Get title content");
              complexParts.add(t.getContent().get(0) + ". ");
            } catch (Exception missingHeader) {
              System.out.println("No section headers.");
            }
            System.out.println("Get paragraph content");
            P secPar = (P) s.getAddressesAndAlternativesAndArraies().get(0);
            List<Serializable> par = secPar.getContent();
            complexParts.add(unfancy(par));
          });
          res = unfancy(complexParts);
        }
      } catch (Exception ex) {
        System.out.println("Exception trying to get abstract, returning error " + ex);
        res = "[Error retrieving abstract for from article.]";
      }
      System.out.println("RES " + res);
      return res;
    }
    throw new RuntimeException("Attempt to get abstract for non-present article " + paperID);
  }

  /**
   *
   * @return true if the article is found
   */
  public boolean isPresent() {
    return get().isPresent();
  }

  /**
   *
   * @return a path to the NXML version of the article
   */
  protected Optional<Path> getNXMLVersion() {
    return listArchiveFiles()
            .stream()
            .filter((p) -> p.toString().toLowerCase().endsWith(".nxml")).
            limit(1).findFirst();
  }

  private Article article() {
    assert isPresent() : "article should only be called for present paper";
    return get().get();
  }

  private void checkForDtds() throws IOException {
    Path fileToCheck = Paths.get(basedir, "NLMOpenAccess",
            "DTD/JATS", "JATS-archivearticle1.dtd");
    if (Files.notExists(fileToCheck, LinkOption.NOFOLLOW_LINKS)) {
      
      Files.createDirectories(fileToCheck.getParent());
      throw new RuntimeException("Download and unpack"
              + " ftp://ftp.ncbi.nlm.nih.gov/pub/jats/archiving/1.0/jats-archiving-dtd-1.0.zip "
              + "into " + fileToCheck.getParent());
    }
  }

  //Scans through DTDs trying to understand the paper.
  // if you need different DTDs look in http://jats.nlm.nih.gov/versions.html
  // to download them; look in ftp://ftp.ncbi.nih.gov/pub/archive_dtd/archiving/
  // BEWARE: it's hoping to use the JATS XSD and therefore the JATS classes from 
  // JAXB; this is probably fixable with complexity, if it causes problems, but it may be better
  // to avoid having to use explicit class names everywere to distinguish
  // e.g. two varieties of Title.
  private Optional<Article> get() {
    if (paper != null) {
      return Optional.of(paper);
    }
    List<String> dtdsToCheck = Arrays.asList(new String[]{"JATS", "NLM2.3"});
    for (String dtd : dtdsToCheck) {
      Path dtdD = Paths.get(basedir, "NLMOpenAccess",
              "DTD", dtd);
      System.out.println("Trying " + dtdD);
      Optional<Article> result = get(dtdD);
      if (result.isPresent()) {
        paper = result.get();
        return result;
      }
    }
    paper = null;
    return Optional.empty();
  }

  private Optional<Article> get(Path dtdD) {
    try {
      Optional<Path> xmlpath = getNXMLVersion();
      if (xmlpath.isPresent()) {
        Path destinationD = Paths.get(basedir, "NLMOpenAccess",
                "NXML");
        if (Files.notExists(destinationD, LinkOption.NOFOLLOW_LINKS)) {
          Files.createDirectory(destinationD);
        }
        checkForDtds();
        Path extractTo = Paths.get(basedir, "NLMOpenAccess",
                "NXML", xmlpath.get().getFileName().toString());
        Files.copy(xmlpath.get(),
                extractTo,
                StandardCopyOption.REPLACE_EXISTING);
        // create JAXB context and initializing Marshaller  
        JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        // specify the location and name of xml file to be read  
        File XMLfile = extractTo.toFile();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        //spf.setXIncludeAware(true);
        //    spf.setNamespaceAware(true);
        //    spf.setValidating(true); // Not required for JAXB/XInclude
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        // spf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, true);
        XMLReader xr = (XMLReader) spf.newSAXParser().getXMLReader();
        SAXSource source = new SAXSource(xr, new InputSource(new FileInputStream(XMLfile)));
        // resolve the DTDs relative to the copy dir
        String toURI = dtdD.toUri().toString();
        source.setSystemId(toURI);
        // this will create Java object - country from the XML file  
        Article thisPaper = (Article) jaxbUnmarshaller.unmarshal(source);
        //jaxbUnmarshaller.unmarshal(XMLfile);
        paper = thisPaper;

        System.out.println("Article Type: " + thisPaper.getArticleType());
        System.out.println("Title: " + getTitle());
        System.out.println("Authors: " + getAuthors().toString());
        System.out.println("Journal: " + getJournalTitle());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Publication date: " + formatter.format(getPublicationDate()));
        System.out.println("Publisher: " + getPublisher());
        System.out.println("Pub info: " + getPubInfo());

        if (thisPaper == null) {
          return Optional.empty();
        }
        return Optional.of(paper);
      }
    } catch (JAXBException e) {
      System.out.println("XML exception " + e);
      System.out.println("1 Path was " + dtdD + " will try next;");
      return Optional.empty();

    } catch (IOException | ParserConfigurationException | SAXException ex) {
      System.out.println("2 Path was " + dtdD + " will try next;");
      return Optional.empty();
      // Logger.getLogger(PubMedOpenAccessPaper.class.getName()).log(Level.SEVERE, null, ex);
    }

    return Optional.empty();
  }
//    /*
//  
//  
//     <!DOCTYPE article PUBLIC "-//NLM//DTD JATS (Z39.96) Journal Archiving and Interchange DTD v1.0 20120330//EN" "JATS-archi
//     vearticle1.dtd">
//     <!DOCTYPE article PUBLIC "-//NLM//DTD Journal Archiving and Interchange DTD v2.3 20070202//EN" "archivearticle.dtd">
//
//     */

  /**
   *
   * @return the ID for this paper. Probably something like PMC3711467
   */
  public String getPaperID() {
    return paperID;
  }
}
