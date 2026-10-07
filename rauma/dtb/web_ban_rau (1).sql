-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 01, 2026 lúc 09:34 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `web_ban_rau`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `chitietdonhang`
--

CREATE TABLE `chitietdonhang` (
  `MaDH` int(11) NOT NULL,
  `MaSP` int(11) NOT NULL,
  `SoLuong` decimal(10,2) NOT NULL CHECK (`SoLuong` > 0),
  `DonGia` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `chitietdonhang`
--

INSERT INTO `chitietdonhang` (`MaDH`, `MaSP`, `SoLuong`, `DonGia`) VALUES
(101, 1, 2.00, 5000.00),
(101, 4, 1.00, 19000.00),
(101, 7, 1.00, 22000.00),
(102, 5, 2.00, 10000.00),
(102, 9, 2.00, 13500.00),
(103, 1, 2.00, 5000.00),
(103, 2, 1.00, 11300.00),
(103, 10, 1.00, 28000.00),
(104, 7, 1.00, 22000.00),
(105, 7, 1.00, 22000.00),
(106, 7, 5.00, 22000.00),
(107, 7, 5.00, 22000.00),
(108, 7, 5.00, 22000.00),
(109, 7, 5.00, 22000.00),
(110, 7, 3.00, 22000.00),
(111, 7, 1.00, 22000.00),
(112, 7, 1.00, 22000.00),
(113, 7, 1.00, 22000.00);

--
-- Bẫy `chitietdonhang`
--
DELIMITER $$
CREATE TRIGGER `trg_ban_hang` AFTER INSERT ON `chitietdonhang` FOR EACH ROW BEGIN
    UPDATE SanPham SET SoLuongTon = SoLuongTon - NEW.SoLuong WHERE MaSP = NEW.MaSP;
    UPDATE DonHang SET TongTien = TongTien + NEW.SoLuong * NEW.DonGia WHERE MaDH = NEW.MaDH;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `chitietgiohang`
--

CREATE TABLE `chitietgiohang` (
  `MaGH` int(11) NOT NULL,
  `MaSP` int(11) NOT NULL,
  `SoLuong` decimal(10,2) NOT NULL CHECK (`SoLuong` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `chitietphieunhap`
--

CREATE TABLE `chitietphieunhap` (
  `MaPN` int(11) NOT NULL,
  `MaSP` int(11) NOT NULL,
  `SoLuong` decimal(10,2) NOT NULL CHECK (`SoLuong` > 0),
  `GiaNhap` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Bẫy `chitietphieunhap`
--
DELIMITER $$
CREATE TRIGGER `trg_nhap_hang` AFTER INSERT ON `chitietphieunhap` FOR EACH ROW BEGIN
    UPDATE SanPham SET SoLuongTon = SoLuongTon + NEW.SoLuong WHERE MaSP = NEW.MaSP;
    UPDATE PhieuNhap SET TongTien = TongTien + NEW.SoLuong * NEW.GiaNhap WHERE MaPN = NEW.MaPN;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `donhang`
--

CREATE TABLE `donhang` (
  `MaDH` int(11) NOT NULL,
  `MaKH` int(11) DEFAULT NULL,
  `MaNV` int(11) DEFAULT NULL,
  `NgayDat` datetime NOT NULL DEFAULT current_timestamp(),
  `HinhThuc` enum('Online','TaiQuay') NOT NULL DEFAULT 'Online',
  `TrangThai` enum('ChoXacNhan','DangGiao','HoanThanh','DaHuy') NOT NULL DEFAULT 'ChoXacNhan',
  `PhuongThucTT` enum('TienMat','ChuyenKhoan','COD') NOT NULL DEFAULT 'TienMat',
  `DiaChiGiao` varchar(255) DEFAULT NULL,
  `GhiChu` varchar(255) DEFAULT NULL,
  `TongTien` decimal(14,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `donhang`
--

INSERT INTO `donhang` (`MaDH`, `MaKH`, `MaNV`, `NgayDat`, `HinhThuc`, `TrangThai`, `PhuongThucTT`, `DiaChiGiao`, `GhiChu`, `TongTien`) VALUES
(101, 1, 2, '2026-09-29 20:14:27', 'Online', 'HoanThanh', 'COD', '123 Cầu Giấy, HN', 'Giao chiều', 51000.00),
(102, 2, 2, '2026-09-30 20:14:27', 'TaiQuay', 'HoanThanh', 'TienMat', 'Tại quầy', '', 47000.00),
(103, 3, NULL, '2026-10-01 20:14:27', 'Online', 'HoanThanh', 'ChuyenKhoan', '45 Lê Văn Lương, HN', 'Gọi trước khi giao', 49300.00),
(104, NULL, 2, '2026-10-01 20:28:30', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 22.000đ, Tiền thối: 0đ', 22000.00),
(105, NULL, 2, '2026-10-01 20:28:33', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 20.000đ, Tiền thối: 0đ', 22000.00),
(106, NULL, 2, '2026-10-01 20:28:41', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 110.000đ, Tiền thối: 0đ', 110000.00),
(107, NULL, 2, '2026-10-01 20:28:42', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 110.000đ, Tiền thối: 0đ', 110000.00),
(108, NULL, 2, '2026-10-01 20:28:42', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 110.000đ, Tiền thối: 0đ', 110000.00),
(109, NULL, 2, '2026-10-01 20:28:42', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 110.000đ, Tiền thối: 0đ', 110000.00),
(110, NULL, 2, '2026-10-01 20:28:56', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 66.000đ, Tiền thối: 0đ', 66000.00),
(111, NULL, 2, '2026-10-01 20:29:10', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 22.000đ, Tiền thối: 0đ', 22000.00),
(112, NULL, 2, '2026-10-01 20:29:10', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 22.000đ, Tiền thối: 0đ', 22000.00),
(113, NULL, 2, '2026-10-01 20:29:23', 'TaiQuay', 'HoanThanh', 'ChuyenKhoan', 'Tại quầy', 'Bán tại quầy (Chuyển khoản). Tiền khách đưa: 22.000đ, Tiền thối: 0đ', 22000.00);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `giohang`
--

CREATE TABLE `giohang` (
  `MaGH` int(11) NOT NULL,
  `MaKH` int(11) NOT NULL,
  `NgayTao` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `giohang`
--

INSERT INTO `giohang` (`MaGH`, `MaKH`, `NgayTao`) VALUES
(1, 1, '2026-10-01 20:14:33'),
(2, 5, '2026-10-01 20:23:53');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `khachhang`
--

CREATE TABLE `khachhang` (
  `MaKH` int(11) NOT NULL,
  `TenDangNhap` varchar(50) DEFAULT NULL,
  `MatKhau` varchar(255) DEFAULT NULL,
  `HoTen` varchar(100) NOT NULL,
  `SDT` varchar(15) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `DiaChi` varchar(255) DEFAULT NULL,
  `TrangThai` tinyint(1) NOT NULL DEFAULT 1,
  `NgayTao` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `khachhang`
--

INSERT INTO `khachhang` (`MaKH`, `TenDangNhap`, `MatKhau`, `HoTen`, `SDT`, `Email`, `DiaChi`, `TrangThai`, `NgayTao`) VALUES
(1, 'kh01', '123456', 'Lê Văn Khách', '0911111111', '', 'Hà Nội', 1, '2026-10-01 20:14:27'),
(2, 'kh_mai', '123456', 'Phạm Thị Mai', '0922222222', 'mai.pham@gmail.com', 'Đà Nẵng', 1, '2026-10-01 20:14:27'),
(3, 'kh_tuan', '123456', 'Bùi Anh Tuấn', '0933333333', 'tuan.bui@gmail.com', 'Hải Phòng', 1, '2026-10-01 20:14:27'),
(4, NULL, NULL, 'Khách Vãng Lai', NULL, NULL, NULL, 1, '2026-10-01 20:14:27'),
(5, 'qewqewqweq', '$2y$10$Zd7w9lmD09XixRrvJMWZzuw/BR0YjdrlvHG/LhAgdCCtTHpQD73IW', 'qưeqweqweqw', '0333333333', '123213@gmail.com', 'đasadsads', 1, '2026-10-01 20:23:53');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `loaisanpham`
--

CREATE TABLE `loaisanpham` (
  `MaLoai` int(11) NOT NULL,
  `TenLoai` varchar(100) NOT NULL,
  `MoTa` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `loaisanpham`
--

INSERT INTO `loaisanpham` (`MaLoai`, `TenLoai`, `MoTa`) VALUES
(1, 'Rau ăn lá', 'Các loại rau xanh ăn lá tươi non, trồng theo tiêu chuẩn VietGAP'),
(2, 'Củ', 'Các loại củ giàu dinh dưỡng, tươi sạch không chất bảo quản'),
(3, 'Quả', 'Các loại quả thực phẩm tươi ngon tự nhiên'),
(4, 'Nấm các loại', 'Nấm tươi bổ dưỡng thu hoạch hàng ngày'),
(5, 'Trái cây', 'Trái cây nhiệt đới tươi ngọt tự nhiên'),
(6, 'Rau gia vị', 'Các loại rau mùi, thơm, gia vị nêm nếm món ăn');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `nhacungcap`
--

CREATE TABLE `nhacungcap` (
  `MaNCC` int(11) NOT NULL,
  `TenNCC` varchar(150) NOT NULL,
  `SDT` varchar(15) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `DiaChi` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `nhacungcap`
--

INSERT INTO `nhacungcap` (`MaNCC`, `TenNCC`, `SDT`, `Email`, `DiaChi`) VALUES
(1, 'HTX Rau Sạch Mộc Châu', '0910000001', 'mocchau_rau@gmail.com', 'Mộc Châu, Sơn La'),
(2, 'Nông Trại Xanh Đà Lạt', '0920000002', 'dalat_farm@gmail.com', 'Đơn Dương, Lâm Đồng'),
(3, 'Trang Trại Hữu Cơ Ba Vì', '0930000003', 'bavi_organic@gmail.com', 'Ba Vì, Hà Nội'),
(4, 'Hợp Tác Xã Nấm Miền Nam', '0940000004', 'nam_miennam@gmail.com', 'Củ Chi, TP. Hồ Chí Minh'),
(5, '423432', '', '', ''),
(6, '11', '', '', '');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `nhanvien`
--

CREATE TABLE `nhanvien` (
  `MaNV` int(11) NOT NULL,
  `TenDangNhap` varchar(50) NOT NULL,
  `MatKhau` varchar(255) NOT NULL,
  `VaiTro` enum('NhanVien','QuanLy') NOT NULL DEFAULT 'NhanVien',
  `HoTen` varchar(100) NOT NULL,
  `NgaySinh` date DEFAULT NULL,
  `GioiTinh` enum('Nam','Nu','Khac') DEFAULT NULL,
  `SDT` varchar(15) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `DiaChi` varchar(255) DEFAULT NULL,
  `NgayVaoLam` date DEFAULT NULL,
  `TrangThai` tinyint(1) NOT NULL DEFAULT 1,
  `NgayTao` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `nhanvien`
--

INSERT INTO `nhanvien` (`MaNV`, `TenDangNhap`, `MatKhau`, `VaiTro`, `HoTen`, `NgaySinh`, `GioiTinh`, `SDT`, `Email`, `DiaChi`, `NgayVaoLam`, `TrangThai`, `NgayTao`) VALUES
(1, 'admin', '123456', 'QuanLy', 'Nguyễn Văn Quản', '1985-10-12', 'Nam', '0900000001', 'admin@gmail.com', 'Hà Nội', NULL, 1, '2026-10-01 20:14:27'),
(2, 'nv_lan', '123456', 'NhanVien', 'Trần Thị Lan', '1995-05-20', 'Nu', '0900000002', NULL, 'Quận 3, TP.HCM', '2022-03-15', 1, '2026-10-01 20:14:27'),
(3, 'nv_hung', '123456', 'NhanVien', 'Lê Hùng', '1992-11-08', 'Nam', '0900000003', NULL, 'Quận Gò Vấp, TP.HCM', '2023-01-10', 1, '2026-10-01 20:14:27');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `phieunhap`
--

CREATE TABLE `phieunhap` (
  `MaPN` int(11) NOT NULL,
  `MaNCC` int(11) NOT NULL,
  `MaNV` int(11) DEFAULT NULL,
  `NgayNhap` datetime NOT NULL DEFAULT current_timestamp(),
  `TongTien` decimal(14,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `sanpham`
--

CREATE TABLE `sanpham` (
  `MaSP` int(11) NOT NULL,
  `TenSP` varchar(150) NOT NULL,
  `MaLoai` int(11) NOT NULL,
  `MaNCC` int(11) DEFAULT NULL,
  `DonViTinh` varchar(20) NOT NULL DEFAULT 'kg',
  `GiaBan` decimal(12,2) NOT NULL CHECK (`GiaBan` >= 0),
  `SoLuongTon` decimal(10,2) NOT NULL DEFAULT 0.00 CHECK (`SoLuongTon` >= 0),
  `MoTa` text DEFAULT NULL,
  `HinhAnh` varchar(255) DEFAULT NULL,
  `TrangThai` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `sanpham`
--

INSERT INTO `sanpham` (`MaSP`, `TenSP`, `MaLoai`, `MaNCC`, `DonViTinh`, `GiaBan`, `SoLuongTon`, `MoTa`, `HinhAnh`, `TrangThai`) VALUES
(1, 'Cải thìa tươi', 1, 1, '300g', 5000.00, 81.00, 'Cải thìa tươi ngon, lá giòn ngọt, giàu vitamin C và chất xơ.', 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=400', 1),
(2, 'Đọt su su Đà Lạt', 1, 2, '300g', 11300.00, 44.00, 'Đọt su su non tươi giòn, xào tỏi thơm ngon.', 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&q=80&w=400', 1),
(3, 'Ngò rí tươi sạch', 6, 1, '50g', 4300.00, 60.00, 'Ngò rí thơm nức, trang trí và gia vị hoàn hảo.', 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&q=80&w=400', 1),
(4, 'Xà lách thủy canh', 1, 2, '200g', 19000.00, 29.00, 'Xà lách thủy canh giòn ngọt mọng nước, làm salad.', 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?auto=format&fit=crop&q=80&w=400', 1),
(5, 'Rau muống Mộc Châu', 1, 1, '500g', 10000.00, 108.00, 'Rau muống non ngon giòn xốp.', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&q=80&w=400', 1),
(6, 'Cà rốt Đà Lạt', 2, 2, '500g', 18000.00, 65.00, 'Cà rốt củ to đều, ngọt thanh.', 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?auto=format&fit=crop&q=80&w=400', 1),
(7, 'Cà chua beef', 3, 2, '500g', 22000.00, 21.00, 'Cà chua mọng nước, làm salad hoặc súp.', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&q=80&w=400', 1),
(8, 'Khoai tây vàng', 2, 2, '500g', 25000.00, 40.00, 'Khoai tây dẻo bùi.', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&q=80&w=400', 1),
(9, 'Nấm kim châm Hàn', 4, 4, '150g', 13500.00, 53.00, 'Nấm kim châm trắng tinh, giòn ngọt.', 'https://images.unsplash.com/photo-1504472478235-9bc48ba4d60f?auto=format&fit=crop&q=80&w=400', 1),
(10, 'Chuối già Nam Mỹ', 5, 2, '1 nải', 1000.00, 34.00, 'Chuối chín vàng đều thơm ngát.', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&q=80&w=400', 1);

-- --------------------------------------------------------

--
-- Cấu trúc đóng vai cho view `v_doanh_thu_ngay`
-- (See below for the actual view)
--
CREATE TABLE `v_doanh_thu_ngay` (
`Ngay` date
,`SoDon` bigint(21)
,`DoanhThu` decimal(36,2)
);

-- --------------------------------------------------------

--
-- Cấu trúc đóng vai cho view `v_doanh_thu_thang`
-- (See below for the actual view)
--
CREATE TABLE `v_doanh_thu_thang` (
`Nam` int(4)
,`Thang` int(2)
,`DoanhThu` decimal(36,2)
);

-- --------------------------------------------------------

--
-- Cấu trúc đóng vai cho view `v_sp_ban_chay`
-- (See below for the actual view)
--
CREATE TABLE `v_sp_ban_chay` (
`MaSP` int(11)
,`TenSP` varchar(150)
,`TongBan` decimal(32,2)
,`DoanhThu` decimal(44,4)
);

-- --------------------------------------------------------

--
-- Cấu trúc đóng vai cho view `v_ton_kho_thap`
-- (See below for the actual view)
--
CREATE TABLE `v_ton_kho_thap` (
`MaSP` int(11)
,`TenSP` varchar(150)
,`SoLuongTon` decimal(10,2)
);

-- --------------------------------------------------------

--
-- Cấu trúc cho view `v_doanh_thu_ngay`
--
DROP TABLE IF EXISTS `v_doanh_thu_ngay`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_doanh_thu_ngay`  AS SELECT cast(`donhang`.`NgayDat` as date) AS `Ngay`, count(0) AS `SoDon`, sum(`donhang`.`TongTien`) AS `DoanhThu` FROM `donhang` WHERE `donhang`.`TrangThai` = 'HoanThanh' GROUP BY cast(`donhang`.`NgayDat` as date) ;

-- --------------------------------------------------------

--
-- Cấu trúc cho view `v_doanh_thu_thang`
--
DROP TABLE IF EXISTS `v_doanh_thu_thang`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_doanh_thu_thang`  AS SELECT year(`donhang`.`NgayDat`) AS `Nam`, month(`donhang`.`NgayDat`) AS `Thang`, sum(`donhang`.`TongTien`) AS `DoanhThu` FROM `donhang` WHERE `donhang`.`TrangThai` = 'HoanThanh' GROUP BY year(`donhang`.`NgayDat`), month(`donhang`.`NgayDat`) ;

-- --------------------------------------------------------

--
-- Cấu trúc cho view `v_sp_ban_chay`
--
DROP TABLE IF EXISTS `v_sp_ban_chay`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_sp_ban_chay`  AS SELECT `sp`.`MaSP` AS `MaSP`, `sp`.`TenSP` AS `TenSP`, sum(`ct`.`SoLuong`) AS `TongBan`, sum(`ct`.`SoLuong` * `ct`.`DonGia`) AS `DoanhThu` FROM ((`chitietdonhang` `ct` join `donhang` `dh` on(`dh`.`MaDH` = `ct`.`MaDH` and `dh`.`TrangThai` = 'HoanThanh')) join `sanpham` `sp` on(`sp`.`MaSP` = `ct`.`MaSP`)) GROUP BY `sp`.`MaSP`, `sp`.`TenSP` ORDER BY sum(`ct`.`SoLuong`) DESC ;

-- --------------------------------------------------------

--
-- Cấu trúc cho view `v_ton_kho_thap`
--
DROP TABLE IF EXISTS `v_ton_kho_thap`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_ton_kho_thap`  AS SELECT `sanpham`.`MaSP` AS `MaSP`, `sanpham`.`TenSP` AS `TenSP`, `sanpham`.`SoLuongTon` AS `SoLuongTon` FROM `sanpham` WHERE `sanpham`.`SoLuongTon` < 10 ;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `chitietdonhang`
--
ALTER TABLE `chitietdonhang`
  ADD PRIMARY KEY (`MaDH`,`MaSP`),
  ADD KEY `MaSP` (`MaSP`);

--
-- Chỉ mục cho bảng `chitietgiohang`
--
ALTER TABLE `chitietgiohang`
  ADD PRIMARY KEY (`MaGH`,`MaSP`),
  ADD KEY `MaSP` (`MaSP`);

--
-- Chỉ mục cho bảng `chitietphieunhap`
--
ALTER TABLE `chitietphieunhap`
  ADD PRIMARY KEY (`MaPN`,`MaSP`),
  ADD KEY `MaSP` (`MaSP`);

--
-- Chỉ mục cho bảng `donhang`
--
ALTER TABLE `donhang`
  ADD PRIMARY KEY (`MaDH`),
  ADD KEY `MaKH` (`MaKH`),
  ADD KEY `MaNV` (`MaNV`),
  ADD KEY `idx_ngaydat` (`NgayDat`);

--
-- Chỉ mục cho bảng `giohang`
--
ALTER TABLE `giohang`
  ADD PRIMARY KEY (`MaGH`),
  ADD UNIQUE KEY `MaKH` (`MaKH`);

--
-- Chỉ mục cho bảng `khachhang`
--
ALTER TABLE `khachhang`
  ADD PRIMARY KEY (`MaKH`),
  ADD UNIQUE KEY `TenDangNhap` (`TenDangNhap`);

--
-- Chỉ mục cho bảng `loaisanpham`
--
ALTER TABLE `loaisanpham`
  ADD PRIMARY KEY (`MaLoai`),
  ADD UNIQUE KEY `TenLoai` (`TenLoai`);

--
-- Chỉ mục cho bảng `nhacungcap`
--
ALTER TABLE `nhacungcap`
  ADD PRIMARY KEY (`MaNCC`);

--
-- Chỉ mục cho bảng `nhanvien`
--
ALTER TABLE `nhanvien`
  ADD PRIMARY KEY (`MaNV`),
  ADD UNIQUE KEY `TenDangNhap` (`TenDangNhap`);

--
-- Chỉ mục cho bảng `phieunhap`
--
ALTER TABLE `phieunhap`
  ADD PRIMARY KEY (`MaPN`),
  ADD KEY `MaNCC` (`MaNCC`),
  ADD KEY `MaNV` (`MaNV`);

--
-- Chỉ mục cho bảng `sanpham`
--
ALTER TABLE `sanpham`
  ADD PRIMARY KEY (`MaSP`),
  ADD KEY `MaLoai` (`MaLoai`),
  ADD KEY `MaNCC` (`MaNCC`),
  ADD KEY `idx_tensp` (`TenSP`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `donhang`
--
ALTER TABLE `donhang`
  MODIFY `MaDH` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=114;

--
-- AUTO_INCREMENT cho bảng `giohang`
--
ALTER TABLE `giohang`
  MODIFY `MaGH` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `khachhang`
--
ALTER TABLE `khachhang`
  MODIFY `MaKH` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT cho bảng `loaisanpham`
--
ALTER TABLE `loaisanpham`
  MODIFY `MaLoai` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT cho bảng `nhacungcap`
--
ALTER TABLE `nhacungcap`
  MODIFY `MaNCC` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT cho bảng `nhanvien`
--
ALTER TABLE `nhanvien`
  MODIFY `MaNV` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `phieunhap`
--
ALTER TABLE `phieunhap`
  MODIFY `MaPN` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `sanpham`
--
ALTER TABLE `sanpham`
  MODIFY `MaSP` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `chitietdonhang`
--
ALTER TABLE `chitietdonhang`
  ADD CONSTRAINT `chitietdonhang_ibfk_1` FOREIGN KEY (`MaDH`) REFERENCES `donhang` (`MaDH`) ON DELETE CASCADE,
  ADD CONSTRAINT `chitietdonhang_ibfk_2` FOREIGN KEY (`MaSP`) REFERENCES `sanpham` (`MaSP`);

--
-- Các ràng buộc cho bảng `chitietgiohang`
--
ALTER TABLE `chitietgiohang`
  ADD CONSTRAINT `chitietgiohang_ibfk_1` FOREIGN KEY (`MaGH`) REFERENCES `giohang` (`MaGH`) ON DELETE CASCADE,
  ADD CONSTRAINT `chitietgiohang_ibfk_2` FOREIGN KEY (`MaSP`) REFERENCES `sanpham` (`MaSP`);

--
-- Các ràng buộc cho bảng `chitietphieunhap`
--
ALTER TABLE `chitietphieunhap`
  ADD CONSTRAINT `chitietphieunhap_ibfk_1` FOREIGN KEY (`MaPN`) REFERENCES `phieunhap` (`MaPN`) ON DELETE CASCADE,
  ADD CONSTRAINT `chitietphieunhap_ibfk_2` FOREIGN KEY (`MaSP`) REFERENCES `sanpham` (`MaSP`);

--
-- Các ràng buộc cho bảng `donhang`
--
ALTER TABLE `donhang`
  ADD CONSTRAINT `donhang_ibfk_1` FOREIGN KEY (`MaKH`) REFERENCES `khachhang` (`MaKH`) ON DELETE SET NULL,
  ADD CONSTRAINT `donhang_ibfk_2` FOREIGN KEY (`MaNV`) REFERENCES `nhanvien` (`MaNV`) ON DELETE SET NULL;

--
-- Các ràng buộc cho bảng `giohang`
--
ALTER TABLE `giohang`
  ADD CONSTRAINT `giohang_ibfk_1` FOREIGN KEY (`MaKH`) REFERENCES `khachhang` (`MaKH`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `phieunhap`
--
ALTER TABLE `phieunhap`
  ADD CONSTRAINT `phieunhap_ibfk_1` FOREIGN KEY (`MaNCC`) REFERENCES `nhacungcap` (`MaNCC`),
  ADD CONSTRAINT `phieunhap_ibfk_2` FOREIGN KEY (`MaNV`) REFERENCES `nhanvien` (`MaNV`) ON DELETE SET NULL;

--
-- Các ràng buộc cho bảng `sanpham`
--
ALTER TABLE `sanpham`
  ADD CONSTRAINT `sanpham_ibfk_1` FOREIGN KEY (`MaLoai`) REFERENCES `loaisanpham` (`MaLoai`),
  ADD CONSTRAINT `sanpham_ibfk_2` FOREIGN KEY (`MaNCC`) REFERENCES `nhacungcap` (`MaNCC`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
