package rockets.mining;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;

//import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


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

        logger.info("find the " + k + "most active rockets");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        List<Rocket> RocketList = new ArrayList<>();

        for(Launch launch :launches){
            RocketList.add(launch.getLaunchVehicle());
        }
        List<Rocket> topkList= new ArrayList<Rocket>();

        Map<Rocket, Integer> RocketMap =   frequencyOfListElements(RocketList);

        Map<Rocket, Integer> sortedRocketMap = RocketMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        List<Rocket> sortedRocketList = new ArrayList<Rocket> ();
        sortedRocketList.addAll(sortedRocketMap.keySet());
        topkList= sortedRocketList.subList(0,k);

        for (Rocket r : topkList)
        {
            System.out.println(r.getName()+" "+r.getCountry());
        }

        return topkList;
    }


    /**
     * java统计List集合中每个元素出现的次数
     */
    public static Map<Rocket, Integer> frequencyOfListElements(List<Rocket> items) {
        if (items == null || items.size() == 0)
        {System.out.println("NULL22");
            return null;}
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
        return Collections.emptyList();
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
     * @param orbit the orbit
     * @return the country who sends the most payload to the orbit
     */
    public String dominantCountry(String orbit) {
        logger.info("find the most launched country in " + orbit);
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<String,Integer> orbitMap = MostLaunchedCountryOfList((List<Launch>)launches,orbit);
        Map<String, Integer> sortedMap = orbitMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<String> countryList = new ArrayList<>();
        countryList.addAll(sortedMap.keySet());
        return (String) countryList.toArray()[0];
    }
    
    public static Map<String, Integer> MostLaunchedCountryOfList(List<Launch> items,String orbit) {
        if (items == null || items.size() == 0) return null;
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Launch temp : items) {
            if(temp.getOrbit()== orbit){                      //count the amount of launching in one orbit for each countries
                Integer count = map.get(temp);
                map.put(temp.getLaunchVehicle().getCountry(), (count == null) ? 1 : count + 1);
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
        return Collections.emptyList();
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
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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

 
