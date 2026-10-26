package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Recipe;
import com.restaurant.restaurant_management.model.RecipeIngredient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final List<Recipe> recipes = new ArrayList<>();

    public RecipeService() {
        // 1. Slow-Braised Beef (Bò sốt tiêu đen thượng hạng)
        recipes.add(new Recipe(
                7L,
                "Slow-Braised Beef (Bò sốt tiêu đen)",
                "Món Chính",
                "/images/dishes/slow-braised-beef.jpg",
                "20 phút",
                "45 phút",
                "Bơ sữa, Tiêu cay",
                "Dùng đĩa gốm mộc lòng sâu, xếp thịt bò góc trung tâm, rưới đẫm sốt tiêu đen bóng mượt, điểm xuyết nhánh hương thảo nướng than và tỏi nguyên tép confit.",
                "Thịt bò Wagyu áp chảo nhiệt độ cao 220°C tạo lớp vỏ giòn caramen trước khi om lửa nhỏ liu riu cùng rượu vang đỏ và tiêu đen Phú Quốc tươi đập dập.",
                Arrays.asList(
                        new RecipeIngredient("Thịt bò Wagyu / Dẻ sườn Úc", "350g", "Cắt khối vuông 4x4 cm, ướp muối biển và dầu olive"),
                        new RecipeIngredient("Tiêu đen Phú Quốc tươi", "35g", "Đập dập thô, không xay nhuyễn để giữ tinh dầu thơm"),
                        new RecipeIngredient("Rượu vang đỏ Bordeaux", "80ml", "Khử vị ngấy và tạo hậu vị đậm đà"),
                        new RecipeIngredient("Nước hầm xương bò cô đặc", "250ml", "Ninh 24 giờ"),
                        new RecipeIngredient("Bơ lạt Anchor", "30g", "Tạo độ sánh bóng tự nhiên cho sốt")
                ),
                Arrays.asList(
                        "Bước 1: Ướp thịt bò với muối khoáng, dầu olive và 1/2 lượng tiêu đen trong 15 phút.",
                        "Bước 2: Áp chảo thịt bò lửa lớn (sear) các mặt trong chảo gang đến khi vàng nâu sậm.",
                        "Bước 3: Cho bơ, hành tím phi thơm, rót vang đỏ deglaze chảo gang, thêm nước dùng bò cô đặc và lượng tiêu còn lại.",
                        "Bước 4: Hạ lửa nhỏ nhất, đậy nắp om thịt trong 40 phút đến khi thịt mềm tan mà không nát.",
                        "Bước 5: Thử vị, kiểm tra độ sánh của sốt, bài trí ra đĩa và phục vụ nóng ở 70°C."
                )
        ));

        // 2. Seared Sea Bass (Cá chẽm áp chảo sốt chanh leo)
        recipes.add(new Recipe(
                5L,
                "Seared Sea Bass (Cá chẽm áp chảo)",
                "Món Chính",
                "/images/dishes/seared-sea-bass.jpg",
                "15 phút",
                "18 phút",
                "Hải sản, Bơ sữa",
                "Đĩa phẳng đá bazan đen, đặt cá chẽm nghiêng khoe lớp da vàng giòn rụm, vẽ vệt sốt chanh leo vàng óng, ăn kèm măng tây áp chảo và cà chua bi nướng.",
                "Da cá phải được thấm khô hoàn toàn bằng khăn bếp trước khi áp chảo. Dùng xẻng ấn nhẹ mặt da trong 30 giây đầu để da không bị cong vênh.",
                Arrays.asList(
                        new RecipeIngredient("Phi lê cá chẽm biển tươi", "300g", "Để nguyên da, lạng đều không xương"),
                        new RecipeIngredient("Nước cốt chanh leo tươi", "60ml", "Lọc hạt lấy nước cốt sánh"),
                        new RecipeIngredient("Măng tây xanh loại 1", "4 cây", "Cắt gốc già, chần sơ nước đá"),
                        new RecipeIngredient("Bơ lạt Pháp", "25g", "Nấu chảy cùng chanh leo tạo sốt beurre blanc"),
                        new RecipeIngredient("Lá xạ hương (Thyme) & tỏi", "2 nhánh", "Tạo mùi thơm khi rưới bơ (baste)")
                ),
                Arrays.asList(
                        "Bước 1: Khía nhẹ 3 đường chéo trên da cá, thấm khô kiệt nước, ướp chút muối tiêu.",
                        "Bước 2: Cho dầu vào chảo gang đun đến điểm khói nhẹ (190°C), đặt mặt da xuống trước.",
                        "Bước 3: Áp chảo mặt da 4 phút đến khi vàng giòn 80% độ chín của miếng cá.",
                        "Bước 4: Thêm bơ, tỏi, thyme; lật mặt thịt áp chảo tiếp 1.5 phút và liên tục rưới bơ nóng lên bề mặt.",
                        "Bước 5: Nấu sốt chanh leo beurre blanc với kem sữa tươi và bơ lạt, nêm chút mật ong rừng cân bằng vị chua."
                )
        ));

        // 3. Charcoal-Grilled Chicken (Gà nướng than hoa)
        recipes.add(new Recipe(
                6L,
                "Charcoal-Grilled Chicken (Gà nướng lá chanh)",
                "Món Chính",
                "/images/dishes/charcoal-grilled-chicken.jpg",
                "25 phút",
                "22 phút",
                "Mật ong, Gia vị than hoa",
                "Thớt gỗ sồi cao cấp có lót giấy nướng mộc, xếp thịt gà chặt xiên xéo, rải lá chanh bánh tẻ thái chỉ siêu mảnh, đi kèm muối tiêu ớt chanh mắc khén.",
                "Nướng trên than hoa đượm không khói, phết đều hỗn hợp sốt mật ong rừng pha mỡ gà mỗi 3 phút để da gà căng bóng giòn tan mà thịt bên trong mọng nước ngọt tự nhiên.",
                Arrays.asList(
                        new RecipeIngredient("Thịt đùi gà ta rút xương", "400g", "Chọn gà thả đồi thịt săn chắc"),
                        new RecipeIngredient("Mật ong hoa rừng tự nhiên", "30ml", "Phết bề mặt tạo màu hổ phách"),
                        new RecipeIngredient("Lá chanh non bánh tẻ", "8 lá", "Rửa sạch thái chỉ nhuyễn"),
                        new RecipeIngredient("Hạt mắc khén & hạt dổi nướng", "10g", "Rang thơm giã mịn"),
                        new RecipeIngredient("Sốt ướp thảo mộc gia truyền", "45ml", "Gừng sẻ, hành tím, dầu điều, sả cây")
                ),
                Arrays.asList(
                        "Bước 1: Khía mặt trong đùi gà, ướp sốt thảo mộc và hạt mắc khén trong ít nhất 30 phút.",
                        "Bước 2: Chuẩn bị bếp than hoa đạt nhiệt hồng đều 180°C, vỉ nướng sạch quết dầu.",
                        "Bước 3: Đặt thịt nướng mặt da trước, lật đều tay mỗi 3 phút.",
                        "Bước 4: Khi gà chín 85%, quét liên tục 2 lớp mật ong rừng và nướng thêm 2 phút tạo màu cánh gián.",
                        "Bước 5: Để gà nghỉ (rest) 3 phút trước khi cắt lát, rắc lá chanh thái chỉ và dùng nóng."
                )
        ));

        // 4. Fresh Spring Rolls (Gỏi cuốn tôm thịt hữu cơ)
        recipes.add(new Recipe(
                1L,
                "Fresh Spring Rolls (Gỏi cuốn tôm thịt)",
                "Khai Vị",
                "/images/dishes/fresh-spring-rolls.jpg",
                "15 phút",
                "0 phút",
                "Hải sản, Đậu phộng (trong nước chấm)",
                "Đĩa men rạn dài phong cách cung đình, xếp 4 cuộn gỏi đều tăm tắp lộ rõ lưng tôm đỏ au và cọng hẹ xanh ngắt, kèm chén sốt tương đậu phộng xay nhuyễn sánh mịn.",
                "Bánh tráng phơi sương nhúng nước ấm thoa nhẹ, cuốn chặt tay vừa phải để không rách vỏ mà vẫn giữ trọn vẹn độ giòn của rau thơm hữu cơ.",
                Arrays.asList(
                        new RecipeIngredient("Tôm sú biển tươi", "6 con", "Hấp chín tới với sả, bóc vỏ chẻ đôi sống lưng"),
                        new RecipeIngredient("Thịt ba chỉ quế", "150g", "Luộc chín tới ngâm nước đá cắt lát mỏng"),
                        new RecipeIngredient("Bánh tráng gạo phơi sương", "4 lá", "Độ dẻo cao không bị chua"),
                        new RecipeIngredient("Bún tươi sợi nhỏ", "120g", "Chần ráo"),
                        new RecipeIngredient("Rau xà lách, húng quế, kinh giới, hẹ", "80g", "Rau sạch Đà Lạt rửa ráo kiệt nước")
                ),
                Arrays.asList(
                        "Bước 1: Sắp xếp các nguyên liệu gọn gàng theo thứ tự trên mặt bàn inox lạnh.",
                        "Bước 2: Làm ẩm bánh tráng bằng khăn ẩm sạch, trải đều xà lách và rau thơm lên 1/3 bề mặt bánh.",
                        "Bước 3: Thêm bún và thịt ba chỉ lên trên lớp rau.",
                        "Bước 4: Cuộn 1 vòng chặt tay, xếp 3 nửa con tôm ngửa phần lưng đỏ ra ngoài kèm 1 nhánh cọng hẹ dài.",
                        "Bước 5: Gập 2 mép hai bên và cuộn dứt khoát hoàn tất. Cắt đôi xiên xéo và trình bày."
                )
        ));

        // 5. Coconut Panna Cotta (Panna Cotta dừa non sầu riêng)
        recipes.add(new Recipe(
                9L,
                "Coconut Panna Cotta (Panna Cotta dừa non)",
                "Tráng Miệng",
                "/images/dishes/coconut-panna-cotta.jpg",
                "25 phút",
                "0 phút",
                "Sữa tươi, Gelatin",
                "Ly thủy tinh pha lê miệng rộng, lớp panna cotta trắng muốt mềm mịn lắc lư nhẹ, phủ thạch hoa đậu biếc và cơm dừa non nạo sợi mỏng.",
                "Cần kiểm soát nhiệt độ dung dịch sữa kem dừa ở mức 65°C - 70°C khi hòa tan gelatin lá, tuyệt đối không để sôi bùng làm tách lớp chất béo của nước cốt dừa.",
                Arrays.asList(
                        new RecipeIngredient("Nước cốt dừa xiêm tươi", "200ml", "Vắt tươi nguyên chất không pha nước"),
                        new RecipeIngredient("Kem sữa tươi Anchor whipping cream", "200ml", "Tạo độ ngậy béo mịn màng"),
                        new RecipeIngredient("Gelatin lá hữu cơ Pháp", "3 lá", "Ngâm nước đá mềm 10 phút trước khi dùng"),
                        new RecipeIngredient("Đường phèn cô bông", "45g", "Vị ngọt thanh tao tự nhiên"),
                        new RecipeIngredient("Thịt cơm dừa non nạo sợi", "50g", "Giòn sần sật nhẹ nhàng")
                ),
                Arrays.asList(
                        "Bước 1: Ngâm lá gelatin trong nước đá lạnh 10 phút cho nở mềm vắt kiệt.",
                        "Bước 2: Đun whipping cream, nước cốt dừa và đường phèn ở lửa nhỏ đến khi bốc khói nhẹ (65°C), quấy tan đường.",
                        "Bước 3: Tắt bếp, thả gelatin đã vắt ráo vào khuấy tan hoàn toàn.",
                        "Bước 4: Lọc hỗn hợp qua rây mịn 2 lần để loại bỏ bọt khí, rót vào từng ly tráng miệng.",
                        "Bước 5: Đưa vào tủ mát chuyên dụng ở nhiệt độ 4°C trong ít nhất 4 giờ trước khi phục vụ."
                )
        ));
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }

    public Optional<Recipe> findByMenuItemId(Long menuItemId) {
        if (menuItemId == null) return Optional.empty();
        return recipes.stream().filter(r -> r.getMenuItemId().equals(menuItemId)).findFirst();
    }
}
