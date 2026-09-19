import io.github.lfarruda188.quietsort.ItemSorter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Teste da logica de ordenacao. O ponto critico: nada pode sumir nem duplicar. */
public class TestSorter {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testNoItemLoss();
        testMergePartialStacks();
        testHotbarUntouched();
        testOverflowIsRefused();
        testEmptyIsNoop();
        testUnstackablesPreserved();
        testDifferentMetaNotMerged();
        testAlreadySortedIsNotRewritten();
        testRandomizedFuzz();

        System.out.println();
        System.out.println("=== passou: " + passed + " | falhou: " + failed + " ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // --- casos ---

    static void testNoItemLoss() {
        ItemStack[] inv = new ItemStack[27];
        inv[0] = new ItemStack(Material.STONE, 30);
        inv[5] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inv[9] = new ItemStack(Material.STONE, 40);
        inv[20] = new ItemStack(Material.BREAD, 12);
        inv[26] = new ItemStack(Material.DIAMOND, 3);

        Map<String, Integer> before = census(inv, 0, inv.length);
        ItemStack[] out = ItemSorter.sortRegion(inv, 0, inv.length);
        check("ordenacao retornou resultado", out != null);
        if (out == null) return;
        Map<String, Integer> after = census(out, 0, out.length);
        check("nenhum item perdido ou duplicado", before.equals(after));
        check("array mantem o tamanho", out.length == 27);
    }

    static void testMergePartialStacks() {
        ItemStack[] inv = new ItemStack[27];
        inv[0] = new ItemStack(Material.STONE, 30);
        inv[4] = new ItemStack(Material.STONE, 40);
        inv[8] = new ItemStack(Material.STONE, 20);
        // 90 pedras = deve virar 64 + 26 em dois slots
        ItemStack[] out = ItemSorter.sortRegion(inv, 0, inv.length);
        check("merge retornou resultado", out != null);
        if (out == null) return;
        int used = 0;
        for (ItemStack s : out) if (s != null) used++;
        check("90 pedras cabem em 2 slots (obtido: " + used + ")", used == 2);
        check("primeira pilha esta cheia", out[0].getAmount() == 64);
        check("segunda pilha tem o resto", out[1].getAmount() == 26);
        check("total preservado", ItemSorter.countItems(out, 0, out.length) == 90);
    }

    static void testHotbarUntouched() {
        ItemStack[] inv = new ItemStack[36];
        // hotbar (0-8) - nao pode mudar nada
        inv[0] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inv[3] = new ItemStack(Material.BREAD, 5);
        inv[8] = new ItemStack(Material.STONE, 10);
        // mochila (9-35)
        inv[30] = new ItemStack(Material.DIRT, 20);
        inv[12] = new ItemStack(Material.COBBLESTONE, 5);

        ItemStack[] out = ItemSorter.sortRegion(inv, 9, 36);
        check("mochila ordenada", out != null);
        if (out == null) return;
        boolean hotbarIntact = out[0] != null && out[0].getType() == Material.DIAMOND_SWORD
                && out[3] != null && out[3].getType() == Material.BREAD && out[3].getAmount() == 5
                && out[8] != null && out[8].getType() == Material.STONE && out[8].getAmount() == 10
                && out[1] == null && out[2] == null;
        check("HOTBAR intacta (slots 0-8)", hotbarIntact);
        check("mochila foi compactada para o inicio", out[9] != null && out[10] != null);
        check("total da mochila preservado", ItemSorter.countItems(out, 9, 36) == 25);
    }

    static void testOverflowIsRefused() {
        // Caso normal: 3 itens fora de ordem em 3 slots cabem.
        ItemStack[] inv = new ItemStack[3];
        inv[0] = new ItemStack(Material.BREAD, 5);
        inv[1] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inv[2] = new ItemStack(Material.COBBLESTONE, 10);
        check("3 itens fora de ordem em 3 slots cabem", ItemSorter.sortRegion(inv, 0, 3) != null);

        // Caso anormal: pilha maior que o limite do item (200 pedras num slot).
        // Ao normalizar viraria 64+64+64+8 = 4 slots, e so existem 2.
        // O esperado e RECUSAR e deixar o inventario intocado.
        ItemStack[] oversized = new ItemStack[2];
        oversized[0] = new ItemStack(Material.STONE, 200);
        oversized[1] = new ItemStack(Material.DIRT, 10);
        ItemStack[] out2 = ItemSorter.sortRegion(oversized, 0, 2);
        check("recusa quando nao cabe, em vez de descartar item", out2 == null);
        check("inventario original ficou intocado",
                oversized[0].getAmount() == 200 && oversized[1].getAmount() == 10);
    }

    static void testEmptyIsNoop() {
        ItemStack[] inv = new ItemStack[27];
        check("inventario vazio nao e mexido", ItemSorter.sortRegion(inv, 0, 27) == null);
    }

    static void testUnstackablesPreserved() {
        ItemStack[] inv = new ItemStack[10];
        inv[9] = new ItemStack(Material.DIAMOND_SWORD, 1, "sword-alpha");
        inv[4] = new ItemStack(Material.DIAMOND_SWORD, 1, "sword-beta");
        ItemStack[] out = ItemSorter.sortRegion(inv, 0, 10);
        check("2 espadas continuam 2 espadas", out != null && count(out) == 2);
    }

    /**
     * Regressao: inventario que ja esta ordenado nao pode ser reescrito.
     * Reescrever forcava o Geyser a retraduzir tudo e travava o celular.
     */
    static void testAlreadySortedIsNotRewritten() {
        ItemStack[] inv = new ItemStack[27];
        inv[7] = new ItemStack(Material.BREAD, 5);
        inv[2] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inv[14] = new ItemStack(Material.DIRT, 30);
        inv[0] = new ItemStack(Material.COBBLESTONE, 64);

        ItemStack[] first = ItemSorter.sortRegion(inv, 0, 27);
        check("primeira passada organiza", first != null);
        if (first == null) return;

        ItemStack[] second = ItemSorter.sortRegion(first, 0, 27);
        check("segunda passada NAO reescreve (retorna null)", second == null);

        // E o mesmo vale para a regiao da mochila
        ItemStack[] bag = new ItemStack[36];
        bag[0] = new ItemStack(Material.DIAMOND_SWORD, 1);
        bag[20] = new ItemStack(Material.DIRT, 10);
        bag[11] = new ItemStack(Material.COBBLESTONE, 5);
        ItemStack[] pass1 = ItemSorter.sortRegion(bag, 9, 36);
        check("mochila: primeira passada organiza", pass1 != null);
        if (pass1 == null) return;
        check("mochila: segunda passada NAO reescreve", ItemSorter.sortRegion(pass1, 9, 36) == null);
    }

    static void testDifferentMetaNotMerged() {
        // itens com meta diferente (encantamento/nome) nunca podem fundir
        ItemStack[] inv = new ItemStack[10];
        inv[5] = new ItemStack(Material.STONE, 10, "normal");
        inv[2] = new ItemStack(Material.STONE, 10, "renomeada");
        ItemStack[] out = ItemSorter.sortRegion(inv, 0, 10);
        check("metas diferentes nao fundem", out != null && count(out) == 2);
        if (out != null) {
            check("quantidades intactas", out[0].getAmount() == 10 && out[1].getAmount() == 10);
        }
    }

    /** Fuzz: 2000 inventarios aleatorios, a conta tem que fechar em todos. */
    static void testRandomizedFuzz() {
        java.util.Random rnd = new java.util.Random(20260919L);
        Material[] pool = {Material.STONE, Material.DIRT, Material.BREAD, Material.DIAMOND,
                Material.DIAMOND_SWORD, Material.IRON_HELMET, Material.OAK_LOG, Material.COBBLESTONE};
        int problems = 0;
        for (int round = 0; round < 2000; round++) {
            int size = 9 + rnd.nextInt(45);
            ItemStack[] inv = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                if (rnd.nextInt(100) < 55) {
                    Material m = pool[rnd.nextInt(pool.length)];
                    int max = m.getMaxStackSize();
                    int amount = 1 + rnd.nextInt(max);
                    String meta = rnd.nextInt(10) == 0 ? ("m" + rnd.nextInt(3)) : null;
                    inv[i] = new ItemStack(m, amount, meta);
                }
            }
            Map<String, Integer> before = census(inv, 0, size);
            ItemStack[] out = ItemSorter.sortRegion(inv, 0, size);
            if (out == null) {
                continue; // recusou: comportamento seguro e aceitavel
            }
            Map<String, Integer> after = census(out, 0, out.length);
            if (!before.equals(after)) {
                problems++;
                if (problems <= 2) {
                    System.out.println("  DIVERGENCIA na rodada " + round);
                    System.out.println("   antes:  " + before);
                    System.out.println("   depois: " + after);
                }
            }
        }
        check("fuzz de 2000 inventarios sem perda/duplicacao", problems == 0);
    }

    // --- utilitarios ---

    /** Conta quantas unidades existem de cada (tipo+meta). */
    static Map<String, Integer> census(ItemStack[] inv, int from, int to) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = from; i < to; i++) {
            ItemStack s = inv[i];
            if (s == null || s.getAmount() <= 0) continue;
            map.merge(s.toString().replaceAll("x\\d+", ""), s.getAmount(), Integer::sum);
        }
        return map;
    }

    static int count(ItemStack[] inv) {
        int c = 0;
        for (ItemStack s : inv) if (s != null) c++;
        return c;
    }

    static void check(String name, boolean ok) {
        System.out.println((ok ? "  OK   " : "  FALHOU ") + name);
        if (ok) passed++; else failed++;
    }
}
