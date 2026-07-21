package secureauth.application.mapper;

import secureauth.application.dto.PetDTO;
import secureauth.model.Pet;

public final class PetMapper {

    private PetMapper() {
    }

    public static PetDTO toDTO(Pet pet) {
        return new PetDTO(pet.getId(), pet.getOwnerId(), pet.getNombreMascota(), null, pet.getRaza());
    }

    public static Pet toDomain(PetDTO dto) {
        Pet pet = new Pet();
        pet.setId(dto.id() == null ? 0 : dto.id());
        pet.setOwnerId(dto.ownerId() == null ? 0 : dto.ownerId());
        pet.setNombreMascota(dto.name());
        pet.setRaza(dto.breed());
        return pet;
    }
}
