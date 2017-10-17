package Jorge_Tomás;

/**
 * Created by tomas on 09/10/2017.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


public class Crawler {
    private List <String> pagesStandvirtual;
    private List <String> pagesOlx;
    private List <String> advertsStandvirtual;
    private List <String> advertsOlx;
    private List <List<String>> advertDetails;
    private Advertisements advertisements;


    public Crawler() {
        pagesStandvirtual = new ArrayList <>();
        pagesOlx = new ArrayList<>();
        advertsStandvirtual = new ArrayList <>();
        advertsOlx = new ArrayList <>();
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
        System.out.println(advertsStandvirtual);
        System.out.println(advertsOlx);
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
                imageUrl = document.getElementsByClass("offer-photos-thumbs__link").first().attr("abs:data-thumb");

                for (Element detail : advertDetails) {
                    Elements children = detail.children();
                    String detailKey = children.first().text();
                    String detailValue = children.last().text();

                    System.out.println(detailKey + ": "+ detailValue);

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
                        displacement.setValue(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
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
                imageUrl = document.getElementsByClass("photo-handler rel inlblk").first().children().first().attr("abs:src");

                for (Element detail : advertDetails) {
                    Elements children = detail.children();
                    String[] details = children.first().text().split(" ");
                    String detailKey = details[0];
                    String detailValue = details[1];
                    System.out.println(detailKey + ": "+ detailValue);

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
                        displacement.setValue(Integer.parseInt(detailValue.replaceAll("[^\\d]", "")));
                        displacement.setUnits("cm3");
                        new_advert.setDisplacement(displacement);
                    } else if (detailKey.toLowerCase().contains("cor")) {
                        new_advert.setColor(detailValue);
                    }

                }
                Advertisements.Advert.Price price = new Advertisements.Advert.Price();
                price.setValue(Integer.parseInt(advertPrice.replaceAll("[^\\d]", "")));
                price.setUnits("€");
                new_advert.setImageUrl(imageUrl);
                new_advert.setPrice(price);
                new_advert.setUrl(advert);


                advertisements.getAdvert().add(new_advert);

            } catch (IOException e) {
                System.err.println(e.getMessage());
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
            File file = new File("adverts.xml");
            if (file.exists() && !file.isDirectory()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder xmlString = new StringBuilder();

                while((line=bufferedReader.readLine()) != null){
                    xmlString.append(line.trim());
                }
                sendString((xmlString.toString()));

            } else {
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
                crawler.getAdvertLink();
                crawler.getAdvertDetails();
                String xmlString = crawler.marshallList();
                sendString(xmlString);
            }
        } catch (IOException io) {
            System.err.println(io);
        }

        System.out.println("Crawler Terminated");
    }

    public static void sendString(String xmlString) {
        try {
            TopicSender sender = new TopicSender();
            sender.sendToTopic(xmlString);
            System.out.println("Message sent");
            File file = new File("adverts.xml");
            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }
        } catch(NamingException ne) {
            System.out.println("Error sending XML. XML saved as adverts.xml");
        }
    }
}
