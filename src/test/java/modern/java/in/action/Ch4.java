package modern.java.in.action;

import modern.java.in.action.data.Dish;
import modern.java.in.action.data.SampleData;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class Ch4 {
    private List<Dish> menu;

    @Before
    public void setUp() {
        menu = SampleData.getMenu();
    }

    /**
     * Q) 저칼로리(400 칼로리 이하) 요리를 낮은 칼로리 순으로 알려달라.
     */
    @Test
    public void useJava7Style() {
        List<Dish> lowCaloricDishes = new ArrayList<>();

        for (Dish dish: menu) {
            if (dish.getCalories() < 400) {
                lowCaloricDishes.add(dish);
            }
        }

        Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
            @Override
            public int compare(Dish dish1, Dish dish2) {
                return Integer.compare(dish1.getCalories(), dish2.getCalories());
            }
        });

        List<String> lowCaloricDishNames = new ArrayList<>();
        for (Dish dish: lowCaloricDishes) {
            lowCaloricDishNames.add(dish.getName());
        }

        System.out.println(lowCaloricDishNames);
    }

    @Test
    public void useJava8Style() {
        /**
         * 스트림 API 장점
         * - 선언형: 간결함, 가독성 향상
         * - 스트림 파이프라인: 연산을 연결해서 데이터 처리 파이프라인을 만들 수 있음
         * - 병렬화: 성능 향상 (?), 데이터 처리 병렬화시 스레드와 락 걱정을 할 필요 없음 (근거는?)
         */
        List<String> lowCaloricDishNames = menu.stream()
                .filter(dish -> dish.getCalories() < 400)
                .sorted(comparing(Dish::getCalories))
                .map(Dish::getName)
                .collect(toList());

        System.out.println(lowCaloricDishNames);
    }

    /**
     * 스트림?
     * - 데이터 처리 연산을 지원하도록 소스에서 추출된 연속된 요소 (Sequence of elements)
     *
     * 스트림의 두 가지 중요 특징
     * - 연산 파이프라인
     * - 내부 반복
     */
    @Test
    public void useStream() {
        // Q. (300칼로리 이상의) 고칼로리 요리 3개를 찾아라.
        List<Dish> threeHighCaloricDishNames = menu.stream()
                .filter(dish -> dish.getCalories() > 300)
                //.map(Dish::getName)
                .limit(3)
                .collect(toList());

        // 데이터 소스의 순서를 유지하므로 고칼로리 요리 중 가장 높은 순서 3개가 반환되는 것이 아님
        // [pork=800, beef=700, chicken=400]
        System.out.println(threeHighCaloricDishNames);

        // Q. (300칼로리 이상의) 고칼로리 요리 3개를 높은 칼로리 순으로 찾아라.
        List<Dish> threeHighCaloricDishNames2 = menu.stream()
                .filter(dish -> dish.getCalories() > 300)
                .sorted(comparing(Dish::getCalories).reversed())
                //.map(Dish::getName)
                .limit(3)
                .collect(toList());

        // [pork=800, beef=700, pizza=550]
        System.out.println(threeHighCaloricDishNames2);
    }

    @Test(expected = IllegalStateException.class)
    public void useStream_한_번만_탐색_할_수_있음() {
        List<String> title = Arrays.asList("Java8", "In", "Action");
        Stream<String> s = title.stream();

        s.forEach(System.out::println);
        s.forEach(System.out::println);
    }

    /**
     * [컬렉션 vs 스트림]
     * 1. 데이터 계산 시점
     * - 컬렉션: 모든 요소는 컬렉션에 추가하기 전에 계산되어야 함
     * - 스트림: 사용자가 데이터를 요청할 때 값을 계산
     *
     * 2. 데이터 반복 처리방법
     * - 컬렉션: 외부 반복 > 사용자가 직접 요소를 반복 (명시적으로 컬렉션 항목을 하나씩 가져와서 처리)
     * - 스트림: 내부 반복 > 반복을 스트림 내부에서 처리 (반복을 숨겨주는 연산 리스트를 제공)
     */
    @Test
    public void useForEach_외부반복() {
        List<String> names = new ArrayList<>();
        for (Dish dish: menu) {
            names.add(dish.getName());
        }

        System.out.println(names);
    }

    @Test
    public void useIterator_외부반복() {
        List<String> names = new ArrayList<>();
        Iterator<Dish> iter = menu.iterator();

        while(iter.hasNext()) {
            Dish dish = iter.next();
            names.add(dish.getName());
        }

        System.out.println(names);
    }

    @Test
    public void useStream_내부반복() {
        List<String> names = menu.stream()
                .map(Dish::getName)
                .collect(toList());

        System.out.println(names);
    }

    @Test
    public void quiz4_1() {
        List<String> highCaloricDishes = new ArrayList<>();
        Iterator<Dish> iter = menu.iterator();
        while (iter.hasNext()) {
            Dish dish = iter.next();
            if (dish.getCalories() > 300) {
                highCaloricDishes.add(dish.getName());
            }
        }

        List<String> fromStream = menu.stream()
                .filter(dish -> dish.getCalories() > 300)
                .map(Dish::getName)
                .collect(toList());
    }

    /**
     * 중간 연산: 연결할 수 있는 스트림 연산 (ex> filter, map, limit, sorted, ...)
     *          스트림 파이프라인에 실행하기 전까지 아무 연산도 수행하지 않는다. (lazy)
     *          lazy > 최적화 기법 적용(short circuit, loop fusion)
     * 최종 연산: 스트림을 닫는 연산 (ex> forEach, collect, ...)
     *          스트림 파이프라인에서 결과를 도출
     *
     * [스트림 사용 과정]
     * 1. 데이터 소스 확보: 질의 수행 대상
     * 2. 스트림 파이프라인 구성: 중간 연산 연결
     * 3. 스트림 파이프라인 실행: 최종 연산을 수행하여 결과 생성
     */
    @Test
    public void useStream_중간연산_처리과정_출력해보기() {
        List<String> names = menu.stream()
                .filter(dish -> {
                    System.out.println("filtering: " + dish.getName());
                    return dish.getCalories() > 300;
                })
                .map(dish -> {
                    System.out.println("mapping: " + dish.getName());
                    return dish.getName();
                })
                .limit(3)
                .collect(toList());

        System.out.println(names);
    }

    @Test
    public void useStream_최종연산() {
        menu.stream().forEach(System.out::println);
    }
}