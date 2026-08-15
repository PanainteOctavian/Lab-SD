def f1():
    str = input("give input: ")
    print(len(str))

def f2():
    first_name = input("give first_name: ")
    last_name = input("give last_name: ")
    print(first_name, last_name)
    print(first_name[0])

def f3():
    str = input("give input: ")
    print(str.endswith('.'))
    print(str.isalpha())
    print('x' in str)
    modif_str = str.replace('e', 'E')
    print(modif_str)

def f4():
    str = input("give input: ")
    print(str[0], str.count(str[0]))
    print(str[-1], str.count(str[-1]))

def f5():
    nr = int(input("give input: "))
    print(nr * (3.14**2))

def f6():
    a = int(input("give input: "))
    b = int(input("give input: "))
    print(a * b)

def f7():
    str = input("give input: ")
    a = int(input("give input: "))
    print(str *  a)

def f8():
    a = int(input("give input: "))
    b = int(input("give input: "))
    print(a**b)

def main():
    f8()

if __name__ == '__main__':
    main()