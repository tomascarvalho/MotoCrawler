package Jorge_Tomás;

/**
 * Created by tomas on 09/10/2017.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


public class Crawler {
    private List <String> pagesStandvirtual;
    private List <String> pagesOlx;
    private List <String> pagesCustoJusto;
    private List <String> advertsStandvirtual;
    private List <String> advertsOlx;
    private List <String> advertsCustoJusto;
    private List <List<String>> advertDetails;
    private Advertisements advertisements;


    public Crawler() {
        pagesStandvirtual = new ArrayList <>();
        pagesOlx = new ArrayList<>();
        pagesCustoJusto = new ArrayList<>();
        advertsStandvirtual = new ArrayList <>();
        advertsOlx = new ArrayList <>();
        advertsCustoJusto = new ArrayList<>();
        advertDetails = new ArrayList <>();
        advertisements = new Advertisements();
    }
    // Gets all the 'destaques' web pages
    public void getWebPagesStandvirtual(String URL) {
        pagesStandvirtual.add(URL);
        try {
            Document document = Jsoup.connect(URL).get();
            Elements otherPages = document.select("a[href^="+ URL + "&page=/]");

            for (Element page : otherPages) {
                String pageHref= page.attr("abs:href");
                if (!pagesStandvirtual.contains(pageHref)) {
                    pagesStandvirtual.add(pageHref);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println(pagesStandvirtual);

    }

    public void getWebPagesOlx(String URL) {
        pagesOlx.add(URL);
        try {
            Document document = Jsoup.connect(URL).get();
            Elements otherPages = document.select("a[href^="+ URL + "&page=/]");

            for (Element page : otherPages) {
                String pageHref= page.attr("abs:href");
                if (!pagesOlx.contains(pageHref)) {
                    pagesOlx.add(pageHref);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println(pagesOlx);

    }
    public void getWebPagesCustoJusto(String URL) {
        pagesCustoJusto.add(URL);
        try {
            Document document = Jsoup.connect(URL).get();
            Elements otherPages = document.select("a[href^=\"http://www.custojusto.pt/portugal/motos?o=\"/]");

            for (Element page : otherPages) {
                String pageHref= page.attr("abs:href");
                if (!pagesCustoJusto.contains(pageHref)) {
                    pagesCustoJusto.add(pageHref);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println(pagesCustoJusto);
    }


    //Connect to each 'destaques' page get all car advert links
    public void getAdvertLink() {

        pagesStandvirtual.forEach(page -> {
            try {
                Document document;
                document = Jsoup.connect(page).get();
                Elements advertBoxes = document.getElementsByClass("offer-item__photo-link");
                for (Element advert : advertBoxes) {
                    String advertHref = advert.attr("abs:href");
                    if (!advertsStandvirtual.contains(advertHref)) {
                        advertsStandvirtual.add(advertHref);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
        pagesOlx.forEach(page -> {
            try {
                Document document;
                document = Jsoup.connect(page).get();
                Elements advertBoxes = document.getElementsByClass("marginright5 link linkWithHash detailsLink");
                for (Element advert : advertBoxes) {
                    String advertHref = advert.attr("abs:href");
                    if (!advertsOlx.contains(advertHref)) {
                        advertsOlx.add(advertHref);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        });
        pagesCustoJusto.forEach(page -> {
            try {
                Document document;
                document = Jsoup.connect(page).get();
                Elements advertBoxes = document.getElementById("dalist").children();
                for (Element advert : advertBoxes) {
                    String advertHref = advert.attr("abs:href");
                    if (!advertsCustoJusto.contains(advertHref) && advertHref.toLowerCase().contains("www.custojusto.pt")) {
                        advertsCustoJusto.add(advertHref);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        });
        System.out.println(advertsStandvirtual);
        System.out.println(advertsOlx);
        System.out.println(advertsCustoJusto);
    }

    //Connect to each advert and scrape the details
    public void getAdvertDetails() {
        advertsStandvirtual.forEach(advert -> {
            Document document;
            try {
                document = Jsoup.connect(advert).get();
                Advertisements.Advert new_advert = new Advertisements.Advert();
                String advertPrice = new String();
                String imageUrl = new String();
                Elements advertDetails = document.getElementsByClass("offer-params__item");
                Elements advertExtras = document.getElementsByClass("offer-features__item");
                advertPrice = document.getElementsByClass("offer-price__number").text();
                try {
                    imageUrl = document.getElementsByClass("offer-photos-thumbs__link").first().attr("abs:data-thumb");
                } catch (Exception e){
                    System.out.println("No photo on advert: ");
                    System.out.println(advert);
                    imageUrl = "http://tutaki.org.nz/wp-content/uploads/2016/04/no-image-available.png";

                }

                for (Element detail : advertDetails) {
                    Elements children = detail.children();
                    String detailKey = children.first().text();
                    String detailValue = children.last().text();

                    if (detailKey.toLowerCase().contains("anunciante")) {
                        new_advert.setAdvertiser(detailValue);
                    } else if (detailKey.toLowerCase().contains("marca")) {
                        new_advert.setBrand(detailValue);
                    } else if (detailKey.toLowerCase().contains("modelo")) {
                        new_advert.setModel(detailValue);
                    } else if (detailKey.toLowerCase().contains("mês")) {
                        new_advert.setMonth(detailValue);
                    } else if (detailKey.toLowerCase().contains("ano")) {
                        new_advert.setYear(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                    } else if (detailKey.toLowerCase().contains("quilómetros")) {
                        Advertisements.Advert.Mileage mileage = new Advertisements.Advert.Mileage();
                        mileage.setValue(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                        mileage.setUnits("km");
                        new_advert.setMileage(mileage);
                    } else if (detailKey.toLowerCase().contains("potência")) {
                        Advertisements.Advert.HorsePower hp = new Advertisements.Advert.HorsePower();
                        hp.setValue(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                        hp.setUnits("cv");
                        new_advert.setHorsePower(hp);
                    } else if (detailKey.toLowerCase().contains("cilindrada")) {
                        Advertisements.Advert.Displacement displacement = new Advertisements.Advert.Displacement();
                        displacement.setValue(Integer.parseInt(detailValue.split("c")[0].replaceAll("[^\\d]", "")));
                        displacement.setUnits("cm3");
                        new_advert.setDisplacement(displacement);
                    } else if (detailKey.toLowerCase().contains("cor")) {
                        new_advert.setColor(detailValue);
                    }
                }
                Advertisements.Advert.Extras extras = new Advertisements.Advert.Extras();

                for (Element extra : advertExtras) {
                    extras.getExtra().add(extra.text());

                }

                new_advert.setExtras(extras);
                Advertisements.Advert.Price price = new Advertisements.Advert.Price();
                price.setValue(Integer.parseInt(advertPrice.replaceAll("[^\\d]", "")));
                price.setUnits("€");
                new_advert.setImageUrl(imageUrl);
                new_advert.setPrice(price);
                new_advert.setUrl(advert);


                advertisements.getAdvert().add(new_advert);

            } catch (IOException e) {
                System.out.println(advert);
                System.err.println(e.getMessage());
            }
        });
        advertsOlx.forEach(advert -> {
            Document document;
            try {
                document = Jsoup.connect(advert).get();
                Advertisements.Advert new_advert = new Advertisements.Advert();
                String advertPrice = new String();
                String imageUrl = new String();
                Elements advertDetails = document.getElementsByClass("item");
                advertPrice = document.getElementsByClass("price-label").text();
                try {
                    imageUrl = document.getElementsByClass("photo-handler rel inlblk").first().children().first().attr("abs:src");
                } catch(Exception e) {
                    System.out.println("Exception occurred: "+ e + " -- On advert: " + advert);
                    imageUrl = "http://tutaki.org.nz/wp-content/uploads/2016/04/no-image-available.png";
                }
                for (Element detail : advertDetails) {
                    Elements children = detail.children();
                    String[] details = children.first().text().split(" ");
                    String detailKey = details[0];
                    String detailValue = details[1];

                    try {

                        if (detailKey.toLowerCase().contains("anunciante")) {
                            new_advert.setAdvertiser(detailValue);
                        } else if (detailKey.toLowerCase().contains("marca")) {
                            new_advert.setBrand(detailValue);
                        } else if (detailKey.toLowerCase().contains("modelo")) {
                            new_advert.setModel(detailValue);
                        } else if (detailKey.toLowerCase().contains("mês")) {
                            new_advert.setMonth(detailValue);
                        } else if (detailKey.toLowerCase().contains("ano")) {
                            new_advert.setYear(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                        } else if (detailKey.toLowerCase().contains("quilómetros")) {
                            Advertisements.Advert.Mileage mileage = new Advertisements.Advert.Mileage();
                            mileage.setValue(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                            mileage.setUnits("km");
                            new_advert.setMileage(mileage);
                        } else if (detailKey.toLowerCase().contains("potência")) {
                            Advertisements.Advert.HorsePower hp = new Advertisements.Advert.HorsePower();
                            hp.setValue(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                            hp.setUnits("cv");
                            new_advert.setHorsePower(hp);
                        } else if (detailKey.toLowerCase().contains("cilindrada")) {
                            Advertisements.Advert.Displacement displacement = new Advertisements.Advert.Displacement();
                            displacement.setValue(Integer.parseInt(detailValue.split("c")[0].replaceAll("[^\\d]", "")));
                            displacement.setUnits("cm3");
                            new_advert.setDisplacement(displacement);
                        } else if (detailKey.toLowerCase().contains("cor")) {
                            new_advert.setColor(detailValue);
                        }
                    } catch (NumberFormatException nfe) {
                        System.out.println(advert + ": " + nfe);
                    }

                }
                Advertisements.Advert.Price price = new Advertisements.Advert.Price();
                try {
                    price.setValue(Integer.parseInt(advertPrice.replaceAll("[^\\d]", "")));
                } catch(NumberFormatException nfe) {
                    System.out.println(advert);
                }
                price.setUnits("€");
                new_advert.setImageUrl(imageUrl);
                new_advert.setPrice(price);
                new_advert.setUrl(advert);


                advertisements.getAdvert().add(new_advert);

            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.out.println(advert);
            }
        });

        advertsCustoJusto.forEach(advert -> {
            Document document;
            try {
                document = Jsoup.connect(advert).get();
                Advertisements.Advert new_advert = new Advertisements.Advert();
                String advertPrice = new String();
                String imageUrl = new String();
                String advertDescription = document.getElementsByClass("words").first().text();
                if (advertDescription.toLowerCase().contains("mt07") || advertDescription.toLowerCase().contains("mt-07")
                        || advertDescription.toLowerCase().contains("mt09") || advertDescription.toLowerCase().contains("mt-09") || advertDescription.toLowerCase().contains("mt 09")
                        || advertDescription.toLowerCase().contains("mt03") || advertDescription.toLowerCase().contains("mt-03") || advertDescription.toLowerCase().contains("mt 03")
                        || advertDescription.toLowerCase().contains("mt01") || advertDescription.toLowerCase().contains("mt-01") || advertDescription.toLowerCase().contains("mt 01")
                        || advertDescription.toLowerCase().contains("mt07") || advertDescription.toLowerCase().contains("mt-07") || advertDescription.toLowerCase().contains("mt 07")
                        || advertDescription.toLowerCase().contains("mt1") || advertDescription.toLowerCase().contains("mt-1") || advertDescription.toLowerCase().contains("mt 1")
                        || advertDescription.toLowerCase().contains("xsr")) {

                    Elements advertDetails = document.getElementsByClass("list-group-item");
                    advertPrice = document.getElementsByClass("real-price").first().text();
                    try {
                        imageUrl = document.getElementsByClass("img_big active b-greylight").first().children().first().attr("abs:src");
                    } catch (NullPointerException ne) {
                        //No image available
                        System.out.println("No image available on advert: " + advert);
                        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/No_image_3x4.svg/1024px-No_image_3x4.svg.png";
                    }
                    new_advert.setBrand(advertDescription);
                    Advertisements.Advert.Price price = new Advertisements.Advert.Price();
                    price.setValue(Integer.parseInt(advertPrice.replaceAll("[^\\d]", "")));
                    price.setUnits("€");
                    new_advert.setImageUrl(imageUrl);
                    new_advert.setPrice(price);
                    new_advert.setUrl(advert);


                    advertisements.getAdvert().add(new_advert);
                }


            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.out.println(advert);
            }
        });
    }

    public String marshallList() {
        String xmlToString = new String();
        try {
            File file = new File("adverts.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Advertisements.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(advertisements, file);
            jaxbMarshaller.marshal(advertisements, stringWriter);
            jaxbMarshaller.marshal(advertisements, System.out);

            xmlToString = stringWriter.toString();
            return xmlToString;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return xmlToString;
    }


    public static void main(String[] args) {
        try {
            File xmlFile = new File("adverts.xml");
            Crawler crawler = new Crawler();

            crawler.getWebPagesOlx("https://www.olx.pt/carros-motos-e-barcos/motociclos-scooters/yamaha/?search%5Bfilter_enum_modelo%5D%5B0%5D=mt-01&search%5Bdescription%5D=1");
            crawler.getWebPagesStandvirtual("https://www.standvirtual.com/motos/yamaha/mt-01/?search%5Bcountry%5D=");
            crawler.getWebPagesOlx("https://www.olx.pt/carros-motos-e-barcos/motociclos-scooters/yamaha/?search%5Bfilter_enum_modelo%5D%5B0%5D=mt-03&search%5Bdescription%5D=1");
            crawler.getWebPagesStandvirtual("https://www.standvirtual.com/motos/yamaha/mt-07/?search%5Bnew_used%5D=on");
            crawler.getWebPagesOlx("https://www.olx.pt/carros-motos-e-barcos/motociclos-scooters/yamaha/?search%5Bfilter_enum_modelo%5D%5B0%5D=mt-07&search%5Bdescription%5D=1");
            crawler.getWebPagesStandvirtual("https://www.standvirtual.com/motos/yamaha/mt-09/?search%5Bcountry%5D=");
            crawler.getWebPagesOlx("https://www.olx.pt/carros-motos-e-barcos/motociclos-scooters/yamaha/?search%5Bfilter_enum_modelo%5D%5B0%5D=mt-09&search%5Bdescription%5D=1");
            crawler.getWebPagesStandvirtual("https://www.standvirtual.com/motos/yamaha/mt-10/?search%5Bcountry%5D=s");
            crawler.getWebPagesOlx("https://www.olx.pt/carros-motos-e-barcos/motociclos-scooters/yamaha/?search%5Bfilter_enum_modelo%5D%5B0%5D=mt-10&search%5Bdescription%5D=1");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/mt-01");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt+01");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt01");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/mt-03");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt+03");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt07");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt-07");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt+07");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt09");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt-09");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt+09");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt03");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt-03");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt+1");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt-1");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt-1");
            crawler.getWebPagesCustoJusto("http://www.custojusto.pt/portugal/motos/yamaha/q/mt1");
            crawler.getWebPagesStandvirtual("http://www.custojusto.pt/portugal/motos/q/tracer");
            crawler.getAdvertLink();
            crawler.getAdvertDetails();
            String xmlString = crawler.marshallList();

            try
            {
                // if file doesnt exists, then create it
                if (!xmlFile.exists())
                {
                    xmlFile.createNewFile();
                }

                FileWriter fw = new FileWriter(xmlFile.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(xmlString);
                bw.close();
            }
            catch( IOException e )
            {
                System.out.println("Error: " + e);
                e.printStackTrace( );
            }

            if (isValidXML(xmlString, "skeleton.xsd")) {

                //transfor to html
                try {
                    transformXML(xmlFile);
                } catch (TransformerException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException io) {
            System.err.println(io);
        }

        System.out.println("Crawler Terminated");
    }

    //validates the XML against the XSD
    public static boolean isValidXML(String xml, String xsd)
    {
        File schemaFile = new File(xsd);
        Source xmlFile = new StreamSource(new StringReader(xml));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            System.out.println("XML is valid");
            return true;
        } catch (SAXException e) {
            System.out.println("XML is NOT valid because:" + e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return false;
    }

    //function to tranform the xml fie into HTML
    public static void transformXML(File xmlFilename) throws TransformerException, FileNotFoundException {

        //XSL template to do the conversion
        Source xslFile  =  new StreamSource("stylesheet.xsl");
        //xml file to be converted
        Source xmlFile =  new StreamSource(xmlFilename);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        //output file
        OutputStream htmlFile = new FileOutputStream("docs/index.html");
        //apply the transformation
        Transformer transform = transformerFactory.newTransformer(xslFile);
        transform.transform(xmlFile, new StreamResult(htmlFile));
        //return the html transformed file
        System.out.println("Html filed created");
    }
}
