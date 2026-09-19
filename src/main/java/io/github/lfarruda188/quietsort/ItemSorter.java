package io.github.lfarruda188.quietsort;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Logica pura de ordenacao. Nao toca em nenhum inventario direto:
 * recebe um array de ItemStack, devolve outro array do mesmo tamanho.
 *
 * Regra de seguranca central: a contagem total de itens tem que bater
 * antes e depois. Se nao bater, quem chamou descarta o resultado.
 */
public final class ItemSorter {

    private ItemSorter() {
    }

    // Ordem das categorias no inventario ordenado.
    private static final int CAT_BLOCK = 0;
    private static final int CAT_TOOL = 1;
    private static final int CAT_ARMOR = 2;
    private static final int CAT_FOOD = 3;
    private static final int CAT_POTION = 4;
    private static final int CAT_MISC = 5;

    /**
     * Ordena a regiao [from, to) do array e devolve uma copia nova.
     * Slots fora da regiao sao copiados sem alteracao.
     *
     * @return array ordenado, ou null se algo impediu a ordenacao com seguranca
     */
    public static ItemStack[] sortRegion(ItemStack[] original, int from, int to) {
        if (original == null) {
            return null;
        }
        if (from < 0 || to > original.length || from >= to) {
            return null;
        }

        long totalBefore = countItems(original, from, to);
        if (totalBefore == 0) {
            return null; // nada a fazer
        }

        // 1. Recolhe copias de tudo que esta na regiao.
        List<ItemStack> collected = new ArrayList<>();
        for (int i = from; i < to; i++) {
            ItemStack stack = original[i];
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0) {
                continue;
            }
            collected.add(stack.clone());
        }

        // 2. Junta pilhas iguais ate o limite de cada item.
        List<ItemStack> merged = mergeStacks(collected);

        // 3. Ordena por categoria, depois nome, depois quantidade (maior primeiro).
        merged.sort(comparator());

        // 4. Se nao couber de volta na regiao, desiste (nunca descartar item).
        int slots = to - from;
        if (merged.size() > slots) {
            return null;
        }

        // 5. Monta o resultado.
        ItemStack[] result = new ItemStack[original.length];
        System.arraycopy(original, 0, result, 0, original.length);
        for (int i = from; i < to; i++) {
            result[i] = null;
        }
        for (int i = 0; i < merged.size(); i++) {
            result[from + i] = merged.get(i);
        }

        // 6. Rede de seguranca: a conta tem que fechar exatamente.
        long totalAfter = countItems(result, from, to);
        if (totalAfter != totalBefore) {
            return null;
        }

        // 7. Se o resultado e igual ao que ja estava la, nao ha o que gravar.
        //    Isso importa muito no Bedrock: reescrever o inventario obriga o
        //    Geyser a retraduzir todos os itens para o cliente, e sem esta
        //    checagem isso acontecia a cada fechamento, mesmo sem mudanca.
        if (sameRegion(original, result, from, to)) {
            return null;
        }

        return result;
    }

    /** Compara duas regioes slot a slot (tipo, metadados e quantidade). */
    static boolean sameRegion(ItemStack[] a, ItemStack[] b, int from, int to) {
        for (int i = from; i < to; i++) {
            ItemStack x = a[i];
            ItemStack y = b[i];
            if (x == null || x.getType() == Material.AIR) {
                if (y != null && y.getType() != Material.AIR) {
                    return false;
                }
                continue;
            }
            if (y == null || y.getType() == Material.AIR) {
                return false;
            }
            if (x.getAmount() != y.getAmount() || !x.isSimilar(y)) {
                return false;
            }
        }
        return true;
    }

    /** Soma a quantidade de itens na regiao indicada. */
    public static long countItems(ItemStack[] contents, int from, int to) {
        long total = 0;
        for (int i = from; i < to; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    /** Junta pilhas compativeis respeitando o tamanho maximo de cada item. */
    private static List<ItemStack> mergeStacks(List<ItemStack> input) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : input) {
            int remaining = stack.getAmount();
            int max = stack.getMaxStackSize();

            if (max <= 1) {
                // Itens que nao empilham (ferramenta, armadura, etc) entram inteiros.
                out.add(stack);
                continue;
            }

            for (ItemStack existing : out) {
                if (remaining <= 0) {
                    break;
                }
                if (existing.getAmount() >= max) {
                    continue;
                }
                if (!existing.isSimilar(stack)) {
                    continue;
                }
                int space = max - existing.getAmount();
                int move = Math.min(space, remaining);
                existing.setAmount(existing.getAmount() + move);
                remaining -= move;
            }

            while (remaining > 0) {
                ItemStack copy = stack.clone();
                int amount = Math.min(max, remaining);
                copy.setAmount(amount);
                out.add(copy);
                remaining -= amount;
            }
        }
        return out;
    }

    private static Comparator<ItemStack> comparator() {
        return Comparator
                .comparingInt((ItemStack s) -> category(s.getType()))
                .thenComparing(s -> s.getType().name())
                .thenComparing(Comparator.comparingInt(ItemStack::getAmount).reversed());
    }

    /**
     * Categoria do material. Usa so propriedades genericas e padroes de nome,
     * nunca uma lista fixa de materiais -- assim nao quebra quando o Minecraft
     * adiciona ou renomeia item em versao nova.
     */
    static int category(Material material) {
        String name = material.name();

        if (endsWithAny(name, "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS")
                || name.equals("ELYTRA")
                || name.endsWith("_HORSE_ARMOR")
                || name.equals("TURTLE_HELMET")
                || name.equals("WOLF_ARMOR")) {
            return CAT_ARMOR;
        }

        if (endsWithAny(name, "_SWORD", "_AXE", "_PICKAXE", "_SHOVEL", "_HOE", "_SPEAR")
                || name.equals("BOW")
                || name.equals("CROSSBOW")
                || name.equals("TRIDENT")
                || name.equals("SHIELD")
                || name.equals("MACE")
                || name.equals("FISHING_ROD")
                || name.equals("SHEARS")
                || name.equals("FLINT_AND_STEEL")) {
            return CAT_TOOL;
        }

        if (material.isEdible()) {
            return CAT_FOOD;
        }

        if (name.contains("POTION") || name.equals("EXPERIENCE_BOTTLE")) {
            return CAT_POTION;
        }

        if (material.isBlock()) {
            return CAT_BLOCK;
        }

        return CAT_MISC;
    }

    private static boolean endsWithAny(String value, String... suffixes) {
        for (String suffix : suffixes) {
            if (value.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
