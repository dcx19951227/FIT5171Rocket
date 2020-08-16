package rockets.mining;

import com.google.common.collect.Lists;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.neo4j.shell.impl.SystemOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.dataaccess.neo4j.Neo4jDAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static rockets.mining.RocketMiner.countSuccussRateLaunch;


public class RocketMinerUnitTest {
    Logger logger = LoggerFactory.getLogger(RocketMinerUnitTest.class);

    private DAO dao;
    private RocketMiner miner;
    private List<Rocket> rockets;
    private List<LaunchServiceProvider> lsps;
    private List<Launch> launches;


    @BeforeEach
    public void setUp() {
        dao = mock(Neo4jDAO.class);
        miner = spy(new RocketMiner(dao));
        rockets = Lists.newArrayList();

        lsps = Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),
                new LaunchServiceProvider("SpaceX", 2002, "USA"),
                new LaunchServiceProvider("ESA", 1975, "Europe ")

        );

        // index of lsp of each rocket
        int[] lspIndex = new int[]{0, 0, 0, 1, 1,0,1,2,0,1};
        // 5 rockets
        for (int i = 0; i < 3; i++) {
            rockets.add(new Rocket("rocket_" + i, "USA", lsps.get(lspIndex[i])));
        }
        for (int i = 4; i < 5; i++) {
            rockets.add(new Rocket("rocket_" + i, "CHN", lsps.get(lspIndex[i])));
        }
        // month of each launch
        int[] months = new int[]{1, 6, 4, 3, 4, 11, 6, 5, 12, 5};

        // index of rocket of each launch
        int[] rocketIndex = new int[]{0, 0, 0, 0, 1, 1, 1, 2, 2, 3};
        int[] sucucssorFailed  = {0,1,0,1,0,1,0,0,1,0} ;
        int[] price={152510,2565840,582154,354184,256342,254125,365541,585223,866544,522402};

        // 10 launches
        launches = IntStream.range(0, 10).mapToObj(i -> {
            logger.info("create " + i + " launch in month: " + months[i]);
            Launch l = new Launch();
            l.setLaunchDate(LocalDate.of(2017, months[i], 1));
            l.setLaunchVehicle(rockets.get(rocketIndex[i]));
            l.setLaunchSite("VAFB");
            l.setLaunchServiceProvider(lsps.get(lspIndex[i]));
            l.setOrbit("LEO");
            l.setPrice(new BigDecimal(price[i]));
            l.setLaunchOutcome(sucucssorFailed[i]==0?Launch.LaunchOutcome.SUCCESSFUL:Launch.LaunchOutcome.FAILED);
            spy(l);
            return l;
        }).collect(Collectors.toList());

//        for (Launch l: launches ){
//            System.out.println(l.getLaunchDate()+"   "+l.getLaunchServiceProvider().getName()+ "  "+l.getLaunchVehicle()+ "  "+l.getPrice());
//        }
    }


    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnTopMostRecentLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate()));
        List<Launch> loadedLaunches = miner.mostRecentLaunches(k);
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnTopMostActivateLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);

        List<Rocket> RocketList = new ArrayList<>();

        for(Launch launch :launches){
            RocketList.add(launch.getLaunchVehicle());
        }
        List<Rocket> topkRocketTest= new ArrayList<Rocket>();

        Map<Rocket, Integer> RocketMap =   miner.frequencyOfListElements(RocketList);

        Map<Rocket, Integer> sortedRocketMap = RocketMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        List<Rocket> sortedRocketList = new ArrayList<Rocket> ();
        sortedRocketList.addAll(sortedRocketMap.keySet());
        topkRocketTest= sortedRocketList.subList(0,k);

        List<Rocket> topkActiveRocket= miner.mostLaunchedRockets(k);

        assertEquals(topkActiveRocket.size(),k);
        assertEquals(topkActiveRocket , topkRocketTest);


    }


    /**
     *
     * @param j  j most expensive launches
     * @param k  k most active countries in j most expensive launches
     */
    @ParameterizedTest
    @MethodSource("ExpensiveActiveParametersProvider")
    public void shouldReturnCountryHavemostExpensiveAndActiveLaunches(int j, int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);

        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchPriceComparator = (a, b) -> -a.getPrice().compareTo(b.getPrice());
        List<Launch> expensiveLaunchTest= launches.stream().sorted(launchPriceComparator).limit(k).collect(Collectors.toList());
        launches= expensiveLaunchTest;
        List<Rocket> RocketList = new ArrayList<>();
        for(Launch launch :launches){
            RocketList.add(launch.getLaunchVehicle());
        }
        List<Rocket> topkListTest;
        Map<Rocket, Integer> RocketMap =   miner.frequencyOfListElements(RocketList);
        Map<Rocket, Integer> sortedRocketMap = RocketMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        List<Rocket> sortedRocketList = new ArrayList<> ();
        sortedRocketList.addAll(sortedRocketMap.keySet());
        topkListTest= sortedRocketList.subList(0,k);

        List<Launch> expensiveLaunch = miner.mostExpensiveLaunches(j);
        when(dao.loadAll(Launch.class)).thenReturn(expensiveLaunch);
        List<Rocket> mostExpensiveActiveRocket = miner.mostLaunchedRockets(k);

        verify(dao,times(3)).loadAll(Launch.class);
        verify(miner).mostExpensiveLaunches(j);
        verify(miner).mostLaunchedRockets(k);
        assertEquals(topkListTest.size(),mostExpensiveActiveRocket.size());
        for (int i=0; i <k; i++){
            assertEquals(topkListTest.get(i),mostExpensiveActiveRocket.get(i));
        }

    }

    static Stream<Arguments> ExpensiveActiveParametersProvider(){
        return Stream.of(Arguments.of(5,2),Arguments.of(5, 1));
    }

    @ParameterizedTest
    @MethodSource("revenueParametersProvider")
    public void shouldReturnhighestRevenueLaunchServiceProviders(int k, int year) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);

        Map<LaunchServiceProvider, BigDecimal> revenueMap = miner.CountLaunchedRevenueByYear((List)launches,year);

        Map<LaunchServiceProvider, BigDecimal> sortedRevenueMap = revenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//        for (LaunchServiceProvider lsp: sortedRevenueMap.keySet()){
//            System.out.println(lsp.getName()+"  "+ lsp.getCountry()+" "+sortedRevenueMap.get(lsp));
//        }
        List<LaunchServiceProvider> sortedRevenueList = new ArrayList<> ();
        sortedRevenueList.addAll(sortedRevenueMap.keySet());

        List<LaunchServiceProvider> topKRevenueList =sortedRevenueList.subList(0,k);

        assertEquals(topKRevenueList , miner.highestRevenueLaunchServiceProviders(k,year));
        verify(miner.highestRevenueLaunchServiceProviders(k,year));

    }
    static Stream<Arguments> revenueParametersProvider(){
        return Stream.of(Arguments.of(3,2017),Arguments.of(5, 2017));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnMostReliableLaunchServiceProviders(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<LaunchServiceProvider> topkReliableListTest= new ArrayList<>();
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<LaunchServiceProvider,Double> successRateMap = miner.countSuccussRateLaunch((List)launches);
        Map<LaunchServiceProvider,Double> sortedSuccessRateMap = successRateMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .collect( toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,LinkedHashMap::new));

        List<LaunchServiceProvider> sortedSuccessRateLsit = new ArrayList<> ();
        sortedSuccessRateLsit.addAll(sortedSuccessRateMap.keySet());
        topkReliableListTest= sortedSuccessRateLsit.subList(0,k);
        List<LaunchServiceProvider> topkReliableList=miner.mostReliableLaunchServiceProviders(k);
        assertEquals(topkReliableList.size(),k);
        assertEquals(topkReliableListTest , topkReliableList);


    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnmostExpensiveLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> -a.getPrice().compareTo(b.getPrice()));
        List<Launch> loadedLaunches = miner.mostExpensiveLaunches(k);
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEO"})
    public void shouldReturndominantCountry(String orbit) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        Collection<Launch> launches = dao.loadAll(Launch.class);

//        System.out.println();
//        for (Launch launch : launches){
//            System.out.println(launch.getLaunchVehicle().getCountry()+"   "+launch.getOrbit());
//        }

        Map<String,Integer> orbitMap = miner.countLauncheByOrbit((List<Launch>)launches,orbit);

        Map<String, Integer> sortedMap = orbitMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<String> countryList = new ArrayList<>();
        countryList.addAll(sortedMap.keySet());

        String dominantCountryInAnOrbit = miner.dominantCountry((List<Launch>)launches,orbit);
        assertEquals(countryList.get(0), dominantCountryInAnOrbit);

//        System.out.println(countryList);

    }


}
