package rockets.mining;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;
import rockets.JsonUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

//import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class RocketMiner{
    private static Logger logger = LoggerFactory.getLogger(RocketMiner.class);

    private DAO dao;
    public RocketMiner(DAO dao) {
        this.dao = dao;
    }
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
    public List<LaunchServiceProvider> mostReliableLaunchServiceProviders(int k) throws IOException {
        logger.info("find most " + k + "most reliable launch service");
        List<LaunchServiceProvider> topkList;
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<LaunchServiceProvider,Integer> reliableMap = SuccessfulLaunchedOfList((List)launches);

        //Map<LaunchServiceProvider, Integer> sortedMap = new LinkedHashMap<LaunchServiceProvider, Integer>();
        Map<LaunchServiceProvider, Integer> sortedMap = reliableMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<LaunchServiceProvider> LaunchServiceProviderList = new ArrayList<LaunchServiceProvider> ();
        LaunchServiceProviderList.addAll(sortedMap.keySet());
        topkList= LaunchServiceProviderList.subList(0,k);

//        List<Map.Entry<LaunchServiceProvider, Integer>> entryList = new ArrayList<Map.Entry<LaunchServiceProvider, Integer>>(reliableMap.entrySet());
//        Collections.sort(entryList, new MapValueComparator2());
//        Iterator<Map.Entry<LaunchServiceProvider, Integer>> iter = entryList.iterator();
//        Map.Entry<LaunchServiceProvider, Integer> tmpEntry = null;
//        while (iter.hasNext()) {
//            tmpEntry = iter.next();
//            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
//        }
//        List<LaunchServiceProvider> LaunchServiceProviderList = JsonUtils.json2ListBean(JsonUtils.toJson(sortedMap.keySet()), LaunchServiceProvider.class);
//        topkList =LaunchServiceProviderList.subList(0,k);
//        return Collections.emptyList();
        return  topkList;

    }

    public static Map<LaunchServiceProvider, Integer> SuccessfulLaunchedOfList(List<Launch> items) {
        if (items == null || items.size() == 0) return null;
        Map<LaunchServiceProvider, Integer> map = new HashMap<LaunchServiceProvider, Integer>();
        for (Launch temp : items) {
            if(temp.getLaunchOutcome()== Launch.LaunchOutcome.SUCCESSFUL){                      //count the successful launched frequency for each launch service provider
                Integer count = map.get(temp);
                map.put(temp.getLaunchServiceProvider(), (count == null) ? 1 : count + 1);
            }
        }
        return map;
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
        Map<String,Integer> orbitMap = MostLaunchedCountryOfList((List)launches,orbit);
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(orbitMap.entrySet());
        Collections.sort(entryList, new MapValueComparator4());
        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();

        Map.Entry<String, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        List<String> countryList = (List<String>) sortedMap.keySet();
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
     * @param k the number of launch service provider.
     * @param year the year in request
     * @return the list of k launch service providers who has the highest sales revenue.
     */
    public List<LaunchServiceProvider> highestRevenueLaunchServiceProviders(int k, int year) throws IOException {
        logger.info("find most " + k + "highest revenue among those launch service provider");
        List<LaunchServiceProvider> topkList;
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<LaunchServiceProvider,BigDecimal> revenueMap = MostRevenueLaunchedOfList((List)launches,year);

        Map<LaunchServiceProvider, BigDecimal> sortedMap = new LinkedHashMap<LaunchServiceProvider, BigDecimal>();
        List<Map.Entry<LaunchServiceProvider, BigDecimal>> entryList = new ArrayList<Map.Entry<LaunchServiceProvider, BigDecimal>>(revenueMap.entrySet());
        Collections.sort(entryList, new MapValueComparator3());
        Iterator<Map.Entry<LaunchServiceProvider, BigDecimal>> iter = entryList.iterator();
        Map.Entry<LaunchServiceProvider, BigDecimal> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        List<LaunchServiceProvider> LaunchServiceProviderList = JsonUtils.json2ListBean(JsonUtils.toJson(sortedMap.keySet()), LaunchServiceProvider.class);
        topkList =LaunchServiceProviderList.subList(0,k);
//        return Collections.emptyList();
        return  topkList;
    }

    public static Map<LaunchServiceProvider, BigDecimal> MostRevenueLaunchedOfList(List<Launch> items,int year) {
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


}

class MapValueComparator implements Comparator<Map.Entry<Rocket, Integer>> {

    @Override
    public int compare(Map.Entry<Rocket, Integer> me1, Map.Entry<Rocket, Integer> me2) {
        return me1.getValue().compareTo(me2.getValue());
    }
}

class MapValueComparator2 implements Comparator<Map.Entry<LaunchServiceProvider,Integer>>{

    @Override
    public int compare(Map.Entry<LaunchServiceProvider, Integer> o1, Map.Entry<LaunchServiceProvider, Integer> o2) {
        return o1.getValue().compareTo(o2.getValue());
    }
}

class MapValueComparator3 implements Comparator<Map.Entry<LaunchServiceProvider,BigDecimal>>{

    @Override
    public int compare(Map.Entry<LaunchServiceProvider, BigDecimal> o1, Map.Entry<LaunchServiceProvider, BigDecimal> o2) {
        return o1.getValue().compareTo(o2.getValue());
    }

}

class MapValueComparator4 implements Comparator<Map.Entry<String,Integer>> {

    @Override
    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        return o1.getValue().compareTo(o2.getValue());
    }
}
