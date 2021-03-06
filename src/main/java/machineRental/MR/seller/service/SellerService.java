package machineRental.MR.seller.service;

import machineRental.MR.exception.BindingResultException;
import machineRental.MR.exception.NotFoundException;
import machineRental.MR.seller.model.Seller;
import machineRental.MR.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    public Seller create(Seller seller, BindingResult bindingResult) {

        validateSellerMpkConsistency(seller.getMpk(), null, bindingResult);
        return sellerRepository.save(seller);
    }

    public Page<Seller> search(String mpk, String name, String city, Pageable pageable) {
        return sellerRepository.findByMpkContainingAndNameContainingAndCityContaining(mpk, name, city, pageable);
    }

    public Seller getByMpk(String mpk) {
        Optional<Seller> dbSeller = Optional.ofNullable(sellerRepository.findByMpk(mpk));

        if (!dbSeller.isPresent()) {
            throw new NotFoundException(String.format("Seller with MPK: \'%s\' does not exist", mpk));
        }

        return dbSeller.get();
    }

    public Seller update(Long id, Seller seller, BindingResult bindingResult) {
        Optional<Seller> dbSeller = sellerRepository.findById(id);
        if(!dbSeller.isPresent()) {
            throw new NotFoundException(String.format("Seller with id: \'%s\' does not exist", id));
        }

        validateSellerMpkConsistency(seller.getMpk(), dbSeller.get().getMpk(), bindingResult);

        seller.setId(id);
        return sellerRepository.save(seller);
    }

    private void validateSellerMpkConsistency(String mpk, String currentMpk, BindingResult bindingResult) {
        if(sellerRepository.existsByMpk(mpk) && !mpk.equals(currentMpk)) {
            bindingResult.addError(new FieldError(
                    "seller",
                    "mpk",
                    String.format("Seller with MPK/NIP: \'%s\' already exists", mpk)));
        }

        if(bindingResult.hasErrors()) {
            throw new BindingResultException(bindingResult);
        }
    }

}
