package rockets.mining;

import com.google.common.collect.Lists;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
        miner = new RocketMiner(dao);
        rockets = Lists.newArrayList();

        lsps = Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),
                new LaunchServiceProvider("SpaceX", 2002, "USA"),
                new LaunchServiceProvider("ESA", 1975, "Europe ")

        );

        // index of lsp of each rocket
        int[] lspIndex = new int[]{0, 0, 0, 1, 1,0,1,2,0,1};
        // 5 rockets
        for (int i = 0; i < 5; i++) {
            rockets.add(new Rocket("rocket_" + i, "USA", lsps.get(lspIndex[i])));
        }
        // month of each launch
        int[] months = new int[]{1, 6, 4, 3, 4, 11, 6, 5, 12, 5};

        // index of rocket of each launch
        int[] rocketIndex = new int[]{0, 0, 0, 0, 1, 1, 1, 2, 2, 3};

        // 10 launches
        launches = IntStream.range(0, 10).mapToObj(i -> {
            logger.info("create " + i + " launch in month: " + months[i]);
            Launch l = new Launch();
            l.setLaunchDate(LocalDate.of(2017, months[i], 1));
            l.setLaunchVehicle(rockets.get(rocketIndex[i]));
            l.setLaunchSite("VAFB");
            l.setLaunchServiceProvider(lsps.get(lspIndex[i]));
            l.setOrbit("LEO");
            l.setPrice(new BigDecimal(new DecimalFormat("#.0000").format(Math.random()*1000000)));
            spy(l);
            return l;
        }).collect(Collectors.toList());

        for (Launch l: launches ){
            System.out.println(l.getLaunchDate()+"   "+l.getLaunchServiceProvider().getName()+ "  "+l.getLaunchVehicle()+ "  "+l.getPrice());
        }
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



    @ParameterizedTest
    @ValueSource(ints = {5,1})
    public List<Rocket> shouldReturnCountryHavemostExpensiveAndActiveLaunches(int j, int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> ExpensiveLaunch = miner.mostExpensiveLaunches(j);
        launches= ExpensiveLaunch;
        List<Rocket> mostExpensiveActiveRocket = miner.mostLaunchedRockets(k);
        return mostExpensiveActiveRocket;


    }


    @ParameterizedTest
    @MethodSource("revenueParametersProvider")
    public void shouldReturnhighestRevenueLaunchServiceProviders(int k, int year) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);

        Map<LaunchServiceProvider, BigDecimal> revenueMap = miner.CountLaunchedRevenueByYear((List)launches,year);

        if(revenueMap == null){
            System.out.println("null=================");
        }

        Map<LaunchServiceProvider, BigDecimal> sortedRevenueMap = revenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<LaunchServiceProvider> sortedRevenueList = new ArrayList<> ();
        sortedRevenueList.addAll(sortedRevenueMap.keySet());

        List<LaunchServiceProvider> topKRevenueList =sortedRevenueList.subList(0,k);
        for (LaunchServiceProvider l : sortedRevenueList)
        {
            System.out.println(l.getName()+" "+l.getCountry());
        }
        assertEquals(topKRevenueList , miner.highestRevenueLaunchServiceProviders(k,year));

    }

    static Stream<Arguments> revenueParametersProvider(){
        return Stream.of(Arguments.of(3,2017),Arguments.of(5, 2017));
    }






}