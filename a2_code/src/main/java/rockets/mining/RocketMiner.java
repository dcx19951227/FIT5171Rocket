package rockets.mining;

import org.neo4j.cypher.internal.frontend.v3_2.phases.Do;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;

//import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;


public class RocketMiner {
    private static Logger logger = LoggerFactory.getLogger(RocketMiner.class);
    private DAO dao;

    public RocketMiner(DAO dao) {
        this.dao = dao;
    }

    /**
     * TODO: to be implemented & tested!
     * Returns the top-k most active rockets, as measured by number of completed launches.
     *
     * @param k the number of rockets to be returned.
     * @return the list of k most active rockets.
     */


    public List<Rocket> mostLaunchedRockets(int k) {

        logger.info("find the " + k + " most active rockets");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        List<Rocket> RocketList = new ArrayList<>();

        for(Launch launch :launches){
            RocketList.add(launch.getLaunchVehicle());
        }
        List<Rocket> topkList= new ArrayList<Rocket>();

        Map<Rocket, Integer> RocketMap =   frequencyOfListElements(RocketList);

        Map<Rocket, Integer> sortedRocketMap = RocketMap.entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        List<Rocket> sortedRocketList = new ArrayList<Rocket> ();
        sortedRocketList.addAll(sortedRocketMap.keySet());
        topkList= sortedRocketList.subList(0,k);

//        for (Rocket r : topkList)
//        {
//            System.out.println(r.getName()+" "+r.getCountry());
//        }

        return topkList;

    }


    /**
     * java统计List集合中每个元素出现的次数
     */

    public static Map<Rocket, Integer> frequencyOfListElements(List<Rocket> items) {
        if (items == null || items.size() == 0)          return null;
        Map<Rocket, Integer> map = new HashMap<Rocket, Integer>();
        for (Rocket temp : items) {
            Integer count = map.get(temp);
            map.put(temp, (count == null) ? 1 : count + 1);
        }
        return map;
    }





    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the top-k most reliable launch service providers as measured
     * by percentage of successful launches.
     *
     * @param k the number of launch service providers to be returned.
     * @return the list of k most reliable ones.
     */

    public List<LaunchServiceProvider> mostReliableLaunchServiceProviders(int k) {
        logger.info("find " + k + " most reliable launch service");
        List<LaunchServiceProvider> topkReliableList= new ArrayList<>();
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<LaunchServiceProvider,Double> successRateMap = countSuccussRateLaunch((List)launches);
        Map<LaunchServiceProvider,Double> sortedSuccessRateMap = successRateMap.entrySet().stream().sorted(Entry.comparingByValue())
                .collect( toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,LinkedHashMap::new));

        for (LaunchServiceProvider lsp : sortedSuccessRateMap.keySet())
        {       System.out.println(lsp.getName()+" "+lsp.getCountry()+" "+ sortedSuccessRateMap.get(lsp));   }
        List<LaunchServiceProvider> sortedSuccessRateLsit = new ArrayList<> ();
        sortedSuccessRateLsit.addAll(sortedSuccessRateMap.keySet());
        topkReliableList= sortedSuccessRateLsit.subList(0,k);

        return topkReliableList;

    }


    public static Map<LaunchServiceProvider, Double> countSuccussRateLaunch(List<Launch> launches){
        if (launches == null || launches.size() == 0) return null;
        Map<LaunchServiceProvider, Integer[]> countSuccussFailedLaunchMap = new HashMap<>();
        Map<LaunchServiceProvider,Double> successRateMap= new HashMap<>();

        for (Launch launch : launches) {
            Integer timeOfSuccuss,timeOfFail;
            if(countSuccussFailedLaunchMap.get(launch.getLaunchServiceProvider())==null){
                timeOfSuccuss=0;
                timeOfFail=0;
            }else{
                timeOfSuccuss= countSuccussFailedLaunchMap.get(launch.getLaunchServiceProvider())[0];
                timeOfFail = countSuccussFailedLaunchMap.get(launch.getLaunchServiceProvider())[1];
            }
            if(launch.getLaunchOutcome()== Launch.LaunchOutcome.SUCCESSFUL){
                timeOfSuccuss ++;
            }else {
                timeOfFail ++;
            }
            //count the successful launched times for each launch service provider
            countSuccussFailedLaunchMap.put(launch.getLaunchServiceProvider(), new Integer[]{timeOfSuccuss, timeOfFail});

        }
        for (LaunchServiceProvider lsp: countSuccussFailedLaunchMap.keySet() ){
            successRateMap.put(lsp, ( (double)countSuccussFailedLaunchMap.get(lsp)[0]/ (double)(countSuccussFailedLaunchMap.get(lsp)[0]+ (double)countSuccussFailedLaunchMap.get(lsp)[1])));
            System.out.println("Name:"+lsp.getName()+ " Succuss:"+ countSuccussFailedLaunchMap.get(lsp)[0]+" Failed:"+ countSuccussFailedLaunchMap.get(lsp)[1]);

        }
        for (LaunchServiceProvider lsp: successRateMap.keySet() ){
            System.out.println("Name:"+lsp.getName()+" succuss rate:"+ successRateMap.get(lsp));
        }
        return successRateMap ;
    }



    /**
     * <p>
     * Returns the top-k most recent launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most recent launches.
     */


    public List<Launch> mostRecentLaunches(int k) {
        logger.info("find most recent " + k + " launches");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchDateComparator = (a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate());
        return launches.stream().sorted(launchDateComparator).limit(k).collect(Collectors.toList());
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the dominant country who has the most launched rockets in an orbit.
     *
     *
     * @param launches
     * @param orbit the orbit
     * @return the country who sends the most payload to the orbit
     */

    public String dominantCountry(List<Launch> launches, String orbit) {
        logger.info("find the most launched country in " + orbit);
        Map<String,Integer> orbitMap = countLauncheByOrbit((List<Launch>)launches,orbit);

        Map<String, Integer> sortedMap = orbitMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<String> countryList = new ArrayList<>();
        countryList.addAll(sortedMap.keySet());
        return countryList.get(0);
    }

    public static Map<String, Integer> countLauncheByOrbit(List<Launch> launches,String orbit) {
        if (launches == null || launches.size() == 0) return null;
        Map<String, Integer> dominantCountryMap = new HashMap<String, Integer>();
        for (Launch launch : launches) {
//            System.out.println(launch.getLaunchVehicle().getCountry()+"    Orbit: "+launch.getOrbit());


            Integer count;
            if(launch.getOrbit().equals(orbit)){
                //count the amount of launching in one orbit for each countries
                if (!dominantCountryMap.containsKey(launch.getLaunchVehicle().getCountry())){
                   count=1;
                }else {
                    count=dominantCountryMap.get(launch.getLaunchVehicle().getCountry())+1;
                }
                dominantCountryMap.put(launch.getLaunchVehicle().getCountry(), count);
            }

        }
//        for (String s: dominantCountryMap.keySet()){
//            System.out.println(s+" orbit: "+ dominantCountryMap.get(s));
//        }
        return dominantCountryMap;
    }

    public static Map<LaunchServiceProvider, BigDecimal> CountLauncheyYear(List<Launch> items,int year) {
        if (items == null || items.size() == 0) return null;
        Map<LaunchServiceProvider, BigDecimal> map = new HashMap<LaunchServiceProvider, BigDecimal>();
        for (Launch temp : items) {
            if(temp.getLaunchDate().getYear()== year ){                      //count the successful launched frequency for each launch service provider
                BigDecimal price = map.get(temp);
                map.put(temp.getLaunchServiceProvider(), (price == null) ? temp.getPrice() : temp.getPrice().add(price));
            }
        }
        return map;
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the top-k most expensive launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most expensive launches.
     */
    public List<Launch> mostExpensiveLaunches(int k) {
        logger.info("find most expensive " + k + " launches");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchPriceComparator = (a, b) -> -a.getPrice().compareTo(b.getPrice());
        return launches.stream().sorted(launchPriceComparator).limit(k).collect(Collectors.toList());
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns a list of launch service provider that has the top-k highest
     * sales revenue in a year.
     *
     * @param k    the number of launch service provider.
     * @param year the year in request
     * @return the list of k launch service providers who has the highest sales revenue.
     */


    public List<LaunchServiceProvider> highestRevenueLaunchServiceProviders(int k, int year) {
        logger.info("find most " + k + "highest revenue among those launch service provider");
        List<LaunchServiceProvider> topKRevenueList;
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<LaunchServiceProvider, BigDecimal> revenueMap = CountLaunchedRevenueByYear((List)launches,year);
        Map<LaunchServiceProvider, BigDecimal> sortedRevenueMap = revenueMap.entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<LaunchServiceProvider> sortedRevenueList = new ArrayList<> ();
        sortedRevenueList.addAll(sortedRevenueMap.keySet());
        topKRevenueList =sortedRevenueList.subList(0,k);
//        for (LaunchServiceProvider l : sortedRevenueList)
//        {            System.out.println(l.getName()+" "+l.getCountry());        }

        return  topKRevenueList;
    }

    /**
     *
     * @param items
     * @param year
     * @return Count Launch Revenue By Year
     */

    public static Map<LaunchServiceProvider, BigDecimal> CountLaunchedRevenueByYear(List<Launch> items,int year) {
        if (items == null || items.size() == 0) return null;
        Map<LaunchServiceProvider, BigDecimal> map = new HashMap<LaunchServiceProvider, BigDecimal>();
        for (Launch temp : items) {
            if(temp.getLaunchDate().getYear()== year ){                      //count the successful launched frequency for each launch service provider
                BigDecimal price = map.get(temp);
                map.put(temp.getLaunchServiceProvider(), (price == null) ? temp.getPrice() : temp.getPrice().add(price));
            }
        }

//        for (LaunchServiceProvider lsp: map.keySet() ){
//            System.out.println(lsp.getName());
//        }
        return map;
    }



}



