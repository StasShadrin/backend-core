@import ru.mentee.power.crm.entity.Lead
@import org.springframework.validation.BindingResult

@param Lead lead
@param BindingResult errors = null

@template.layout.main(content = @`
    <div class="max-w-md mx-auto mt-8">
        <h1 class="text-2xl font-bold mb-6">Добавить нового лида</h1>

        <!-- Global Errors -->
        @if(errors != null && errors.hasFieldErrors())
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
                @for (var error : errors.getFieldErrors())
                    <p class="mb-1">${error.getDefaultMessage()}</p>
                @endfor
            </div>
        @endif

        <form action="/jpa-leads" method="post" class="space-y-4" novalidate>

            <!-- Name -->
            <div class="mb-4">
                <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Имя</label>
                <input
                        type="text"
                        id="name"
                        name="name"
                        value="${lead.getName() != null ? lead.getName() : ""}"
                        class="w-full px-3 py-2 border ${errors != null && errors.hasFieldErrors("name") ? "border-red-500" : "border-gray-300"} rounded-md focus:ring-2 focus:ring-blue-500"
                        placeholder="Кто ты воин?"
                />
                @if(errors != null && errors.hasFieldErrors("name"))
                    <p class="text-red-600 text-sm mt-1">${errors.getFieldError("name").getDefaultMessage()}</p>
                @endif
            </div>

            <!-- Email -->
            <div class="mb-4">
                <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                        type="email"
                        id="email"
                        name="email"
                        value="${lead.getEmail() != null ? lead.getEmail() : ""}"
                        class="w-full px-3 py-2 border ${errors != null && errors.hasFieldErrors("email") ? "border-red-500" : "border-gray-300"} rounded-md focus:ring-2 focus:ring-blue-500"
                        placeholder="example@company.com"
                />
                @if(errors != null && errors.hasFieldErrors("email"))
                    <p class="text-red-600 text-sm mt-1">${errors.getFieldError("email").getDefaultMessage()}</p>
                @endif
            </div>

            <!-- Phone -->
            <div class="mb-4">
                <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">Телефон</label>
                <input
                        type="tel"
                        id="phone"
                        name="phone"
                        value="${lead.getPhone() != null ? lead.getPhone() : ""}"
                        class="w-full px-3 py-2 border ${errors != null && errors.hasFieldErrors("phone") ? "border-red-500" : "border-gray-300"} rounded-md focus:ring-2 focus:ring-blue-500"
                        placeholder="+7 (999) 123-45-67"
                />
                @if(errors != null && errors.hasFieldErrors("phone"))
                    <p class="text-red-600 text-sm mt-1">${errors.getFieldError("phone").getDefaultMessage()}</p>
                @endif
            </div>

            <!-- Company -->
            <div class="mb-4">
                <label for="company" class="block text-sm font-medium text-gray-700 mb-1">Компания</label>
                <input
                        type="text"
                        id="company"
                        name="company"
                        value="${lead.getCompany() != null ? lead.getCompany() : ""}"
                        class="w-full px-3 py-2 border ${errors != null && errors.hasFieldErrors("company") ? "border-red-500" : "border-gray-300"} rounded-md focus:ring-2 focus:ring-blue-500"
                        placeholder="Название компании"
                />
                @if(errors != null && errors.hasFieldErrors("company"))
                    <p class="text-red-600 text-sm mt-1">${errors.getFieldError("company").getDefaultMessage()}</p>
                @endif
            </div>

            <!-- Status -->
            <div class="mb-4">
                <label for="status" class="block text-sm font-medium text-gray-700 mb-1">Статус</label>
                <select
                        id="status"
                        name="status"
                        class="w-full px-3 py-2 border ${errors != null && errors.hasFieldErrors("status") ? "border-red-500" : "border-gray-300"} rounded-md focus:ring-2 focus:ring-blue-500"
                >
                    <option value="">Выберите статус</option>
                    <option value="NEW">Новый</option>
                    <option value="CONTACTED">Связались</option>
                    <option value="QUALIFIED">Квалифицирован</option>
                </select>
                @if(errors != null && errors.hasFieldErrors("status"))
                    <p class="text-red-600 text-sm mt-1">${errors.getFieldError("status").getDefaultMessage()}</p>
                @endif
            </div>

            <!-- Submit Button -->
            <button
                    type="submit"
                    class="w-full bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 focus:ring-2 focus:ring-blue-500"
            >
                Создать лида
            </button>

            <!-- Cancel Link -->
            <a
                    href="/jpa-leads"
                    class="block text-center text-sm text-gray-600 hover:text-gray-900"
            >
                Отмена
            </a>
        </form>
    </div>
`)