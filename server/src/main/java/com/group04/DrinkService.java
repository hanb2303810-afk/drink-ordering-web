package com.group04;

import java.util.List;

public class DrinkService {

    private final IGenericRepository<Drink, String> drinkRepository;

    public DrinkService(IGenericRepository<Drink, String> drinkRepository) {
        this.drinkRepository = drinkRepository;
    }

    /**
     * Pre-condition: drink != null, drink.getId() chua ton tai trong repository
     * Post-condition: drink duoc luu vao repository
     * @throws DuplicateKeyException neu id da ton tai
     */
    public Drink createDrink(Drink drink) {
        if (drinkRepository.existsById(drink.getId())) {
            throw new DuplicateKeyException(
                    "Da ton tai do uong voi id: " + drink.getId()
            );
        }

        return drinkRepository.add(drink);
    }

    /**
     * @throws EntityNotFoundException neu khong tim thay
     */
    public Drink getDrink(String id) {
        return drinkRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Khong tim thay do uong voi id: " + id
                        )
                );
    }

    public List<Drink> getAllDrinks() {
        return drinkRepository.findAll();
    }

    /**
     * @throws EntityNotFoundException neu khong tim thay
     */
    public void deleteDrink(String id) {
        drinkRepository.deleteById(id);
    }
    public Drink updateDrink(Drink drink) {
        if (drink == null) {
            throw new ValidationException("Do uong khong duoc null");
        }
        return drinkRepository.update(drink); // tu nem EntityNotFoundException neu chua co id
    }
}