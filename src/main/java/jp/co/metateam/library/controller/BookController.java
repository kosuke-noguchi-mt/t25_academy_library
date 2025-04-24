package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add";//書籍登録画面
    }


    //新しく
    @PostMapping("/book/add")
     public String book_add(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra) {
        try {
            boolean errtitleFlg = false;
            boolean errIsbnFlg = false;
            String title = bookMstDto.getTitle();
            String isbn = bookMstDto.getIsbn();
        
            
            if(title == null || String.valueOf(title).length() == 0 ){
                result.rejectValue("title","error.value","書籍名は必須です");
                errtitleFlg = true;
            }
            
            if(isbn == null || String.valueOf(isbn).length() == 0){
                result.rejectValue("isbn","error.value","ISBNは必須です");
                errIsbnFlg = true;
            }
            
            if(String.valueOf(title).length() >=256){
                result.rejectValue("title","error.value","書籍名は255文字以内で入力してください");
                errtitleFlg = true;
            }
            if(String.valueOf(isbn).length()!=13){
                result.rejectValue("isbn","error.value","ISBNは13文字で入力してください");
                errIsbnFlg = true;
            }

            if(!isbn.matches("^[0-9]*$")){
                result.rejectValue("isbn","error.value","ISBNは半角数字で入力してください");
                errIsbnFlg = true;
            }

            if(result.hasErrors()){
                return "book/add";
            }
            
            int isbnExist = this.bookMstService.selectByIsbn(isbn);

            if(isbnExist >= 1){
                result.rejectValue("isbn", "error.value", "登録済みのISBNです");
                errIsbnFlg = true;
            }
            
            if (errtitleFlg || errIsbnFlg) {
                throw new Exception();           
            }
            
            bookMstService.save(bookMstDto);
        return "redirect:/book/index";       
        }catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("bookMstDto", bookMstDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);

            return "book/add";
        
        }
    }
}