import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// ---------- Problem 1: Username Availability ----------
class UsernameAvailability {
    private final Set<String> registered = ConcurrentHashMap.newKeySet();
    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public UsernameAvailability() {
        registered.addAll(Arrays.asList("john_doe", "admin", "jane_smith"));
    }

    public boolean check(String username) {
        attempts.putIfAbsent(username, new AtomicInteger(0));
        attempts.get(username).incrementAndGet();
        return !registered.contains(username);
    }

    public List<String> suggest(String username) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String candidate = username + i;
            if (!registered.contains(candidate)) suggestions.add(candidate);
        }
        String dotVariant = username.replace("_", ".");
        if (!registered.contains(dotVariant)) suggestions.add(dotVariant);
        return suggestions;
    }

    public String mostAttempted() {
        return attempts.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().get()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}

// ---------- Problem 2: Flash Sale Inventory Manager ----------
class InventoryManager {
    private final Map<String, AtomicInteger> stock = new ConcurrentHashMap<>();
    private final Map<String, Queue<Integer>> waitingList = new ConcurrentHashMap<>();

    public InventoryManager(Map<String, Integer> initialStock) {
        initialStock.forEach((k,v) -> stock.put(k, new AtomicInteger(v)));
    }

    public synchronized boolean purchase(String product, int userId) {
        stock.putIfAbsent(product, new AtomicInteger(0));
        if (stock.get(product).get() > 0) {
            stock.get(product).decrementAndGet();
            return true;
        } else {
            waitingList.putIfAbsent(product, new LinkedList<>());
            waitingList.get(product).offer(userId);
            return false;
        }
    }

    public int checkStock(String product) {
        return stock.getOrDefault(product, new AtomicInteger(0)).get();
    }

    public List<Integer> getWaitingList(String product) {
        return new ArrayList<>(waitingList.getOrDefault(product, new LinkedList<>()));
    }
}

// ---------- Problem 3: DNS Cache with TTL ----------
class DNSCache {
    class Entry {
        String ip; long expiry;
        Entry(String ip, long ttl) { this.ip = ip; this.expiry = System.currentTimeMillis() + ttl*1000; }
    }
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private int hits=0, misses=0;

    public String resolve(String domain) {
        Entry e = cache.get(domain);
        if (e != null && System.currentTimeMillis() < e.expiry) { hits++; return e.ip; }
        misses++;
        // simulate upstream query
        String ip = "1.2.3." + new Random().nextInt(255);
        cache.put(domain, new Entry(ip, 300));
        return ip;
    }
    public void stats() {
        int total = hits + misses;
        System.out.printf("Cache Hit: %.2f%%, Miss: %.2f%%\n", hits*100.0/total, misses*100.0/total);
    }
}

// ---------- Problem 4: Plagiarism Detection ----------
class PlagiarismDetector {
    private final Map<String, Set<String>> ngramIndex = new HashMap<>();
    private int N = 5;

    public void addDocument(String docId, String text) {
        String[] words = text.split("\\s+");
        for (int i = 0; i <= words.length - N; i++) {
            String ngram = String.join(" ", Arrays.copyOfRange(words, i, i+N));
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
        }
    }

    public Map<String, Integer> checkSimilarity(String docId, String text) {
        Map<String, Integer> matches = new HashMap<>();
        String[] words = text.split("\\s+");
        for (int i = 0; i <= words.length - N; i++) {
            String ngram = String.join(" ", Arrays.copyOfRange(words, i, i+N));
            for (String other : ngramIndex.getOrDefault(ngram, Collections.emptySet())) {
                if (!other.equals(docId)) matches.put(other, matches.getOrDefault(other,0)+1);
            }
        }
        return matches;
    }
}

// ---------- Problem 5: Real-time Analytics ----------
class AnalyticsDashboard {
    Map<String, AtomicInteger> pageViews = new ConcurrentHashMap<>();
    Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    Map<String, AtomicInteger> trafficSources = new ConcurrentHashMap<>();

    public void process(String url, String userId, String source) {
        pageViews.putIfAbsent(url, new AtomicInteger(0));
        pageViews.get(url).incrementAndGet();
        uniqueVisitors.computeIfAbsent(url, k->new HashSet<>()).add(userId);
        trafficSources.putIfAbsent(source, new AtomicInteger(0));
        trafficSources.get(source).incrementAndGet();
    }

    public void display() {
        System.out.println("Top Pages:");
        pageViews.entrySet().stream().sorted((a,b)->b.getValue().get()-a.getValue().get())
                .limit(10).forEach(e -> System.out.println(e.getKey()+" - "+e.getValue()+" views, unique: "+uniqueVisitors.get(e.getKey()).size()));
        System.out.println("Traffic Sources:");
        trafficSources.forEach((k,v)-> System.out.println(k+": "+v));
    }
}

// ---------- Problem 6: Rate Limiter ----------
class RateLimiter {
    class Bucket { int tokens; long lastRefill; Bucket(int t){tokens=t; lastRefill=System.currentTimeMillis();} }
    Map<String, Bucket> clients = new ConcurrentHashMap<>();
    int maxTokens = 5; // reduced for demo

    public boolean allow(String clientId) {
        clients.putIfAbsent(clientId, new Bucket(maxTokens));
        Bucket b = clients.get(clientId);
        long now = System.currentTimeMillis();
        long elapsed = now - b.lastRefill;
        if (elapsed > 1000) { // refill every second
            b.tokens = maxTokens;
            b.lastRefill = now;
        }
        if (b.tokens>0) { b.tokens--; return true; }
        return false;
    }
}

// ---------- Problem 7: Autocomplete ----------
class Autocomplete {
    private final Map<String,Integer> freq = new HashMap<>();

    public void addQuery(String query) { freq.put(query, freq.getOrDefault(query,0)+1); }

    public List<String> suggest(String prefix) {
        return freq.entrySet().stream().filter(e->e.getKey().startsWith(prefix))
                .sorted((a,b)->b.getValue()-a.getValue())
                .limit(10).map(Map.Entry::getKey).toList();
    }
}

// ---------- Problem 8: Parking Lot ----------
class ParkingLot {
    class Spot { String plate; Spot(String p){plate=p;} }
    Spot[] lot;
    public ParkingLot(int size){lot=new Spot[size];}
    public int park(String plate){
        int h = Math.abs(plate.hashCode())%lot.length;
        for (int i=0;i<lot.length;i++){
            int idx = (h+i)%lot.length;
            if (lot[idx]==null){lot[idx]=new Spot(plate); return idx;}
        }
        return -1;
    }
    public void exit(int idx){lot[idx]=null;}
}

// ---------- Problem 9: Two-Sum ----------
class TwoSum {
    public List<int[]> findTwoSum(int[] arr, int target){
        Map<Integer,Integer> map = new HashMap<>();
        List<int[]> res = new ArrayList<>();
        for (int i=0;i<arr.length;i++){
            int comp = target-arr[i];
            if(map.containsKey(comp)) res.add(new int[]{map.get(comp), i});
            map.put(arr[i], i);
        }
        return res;
    }
}

// ---------- Problem 10: Multi-Level Cache ----------
class MultiLevelCache {
    LinkedHashMap<String,String> L1 = new LinkedHashMap<>(16,0.75f,true);
    Map<String,String> L2 = new HashMap<>();
    Map<String,String> L3 = new HashMap<>();
    int L1Capacity = 3; // small for demo

    public String get(String key){
        if(L1.containsKey(key)) return L1.get(key);
        if(L2.containsKey(key)) { String val=L2.get(key); promote(key,val); return val;}
        if(L3.containsKey(key)) { String val=L3.get(key); promote(key,val); return val;}
        return null;
    }

    public void promote(String key,String val){
        if(L1.size()>=L1Capacity) { Iterator<String> it=L1.keySet().iterator(); it.next(); it.remove();}
        L1.put(key,val);
    }

    public void put(String key,String val){ L3.put(key,val);}
}

// ---------------- Main Driver ----------------
public class HashTablePracticeAllProblems {
    public static void main(String[] args) {
        System.out.println("----- Problem 1: Username Checker -----");
        UsernameAvailability ua = new UsernameAvailability();
        System.out.println("john_doe available? " + ua.check("john_doe"));
        System.out.println("Suggestions for john_doe: " + ua.suggest("john_doe"));
        System.out.println("Most attempted: " + ua.mostAttempted());

        System.out.println("\n----- Problem 2: Inventory Manager -----");
        InventoryManager inv = new InventoryManager(Map.of("IPHONE15", 2));
        System.out.println("Purchase 1: "+inv.purchase("IPHONE15",101));
        System.out.println("Purchase 2: "+inv.purchase("IPHONE15",102));
        System.out.println("Purchase 3 (waitlist): "+inv.purchase("IPHONE15",103));
        System.out.println("Stock remaining: "+inv.checkStock("IPHONE15"));
        System.out.println("Waiting list: "+inv.getWaitingList("IPHONE15"));

        System.out.println("\n----- Problem 3: DNS Cache -----");
        DNSCache dns = new DNSCache();
        System.out.println("Resolve google.com: "+dns.resolve("google.com"));
        System.out.println("Resolve google.com: "+dns.resolve("google.com"));
        dns.stats();

        System.out.println("\n----- Problem 4: Plagiarism -----");
        PlagiarismDetector pd = new PlagiarismDetector();
        pd.addDocument("doc1","the quick brown fox jumps over the lazy dog");
        pd.addDocument("doc2","the quick brown fox jumps high");
        System.out.println("Similarity: "+pd.checkSimilarity("doc2","the quick brown fox jumps over the lazy dog"));

        System.out.println("\n----- Problem 5: Analytics Dashboard -----");
        AnalyticsDashboard ad = new AnalyticsDashboard();
        ad.process("/home","u1","google"); ad.process("/home","u2","facebook"); ad.display();

        System.out.println("\n----- Problem 6: Rate Limiter -----");
        RateLimiter rl = new RateLimiter();
        System.out.println("Client abc allowed? "+rl.allow("abc"));
        System.out.println("Client abc allowed? "+rl.allow("abc"));

        System.out.println("\n----- Problem 7: Autocomplete -----");
        Autocomplete ac = new Autocomplete();
        ac.addQuery("java tutorial"); ac.addQuery("java script"); ac.addQuery("java tutorial");
        System.out.println("Suggestions for 'java': "+ac.suggest("java"));

        System.out.println("\n----- Problem 8: Parking Lot -----");
        ParkingLot pl = new ParkingLot(5);
        int spot = pl.park("ABC-123"); System.out.println("ABC-123 parked at spot "+spot);
        pl.exit(spot); System.out.println("ABC-123 exited.");

        System.out.println("\n----- Problem 9: Two-Sum -----");
        TwoSum ts = new TwoSum();
        int[] arr = {2,7,11,15}; System.out.println("Two-sum pairs for 9: "+Arrays.deepToString(ts.findTwoSum(arr,9).toArray()));

        System.out.println("\n----- Problem 10: Multi-Level Cache -----");
        MultiLevelCache cache = new MultiLevelCache();
        cache.put("v1","video1"); cache.put("v2","video2"); cache.put("v3","video3"); cache.put("v4","video4");
        System.out.println("Get v1: "+cache.get("v1"));
        System.out.println("Get v2: "+cache.get("v2"));
    }
}